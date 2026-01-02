package com.calendar.controller;

import com.calendar.config.GoogleCalendarConfig;
import com.calendar.service.GoogleCalendarService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Google Calendar OAuth and synchronization endpoints.
 */
@RestController
@RequestMapping("/api/google")
public class GoogleCalendarController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarController.class);

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private GoogleCalendarConfig googleCalendarConfig;

    @org.springframework.beans.factory.annotation.Value("${frontend.url}")
    private String frontendUrl;

    /**
     * Initiate OAuth flow - returns authorization URL.
     */
    @GetMapping("/auth")
    public ResponseEntity<Map<String, String>> initiateAuth(org.springframework.security.core.Authentication authentication) {
        logger.info("InitiateAuth request received");
        if (authentication == null) {
            logger.warn("Unauthenticated attempt to initiate Google Auth");
            return ResponseEntity.status(401).build();
        }
        
        com.calendar.model.User user = (com.calendar.model.User) authentication.getPrincipal();
        Long userId = user.getId();
        logger.info("Initiating Google OAuth for user: {} (ID: {})", user.getUsername(), userId);
        
        String authUrl = googleCalendarService.getAuthorizationUrl(
                googleCalendarConfig.getRedirectUri(), userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("authUrl", authUrl);
        
        logger.info("Generated Google OAuth URL: {}", authUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user is connected to Google Calendar.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getStatus(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        
        com.calendar.model.User principal = (com.calendar.model.User) authentication.getPrincipal();
        // Reload from DB to get fresh token status
        com.calendar.model.User user = googleCalendarService.getUserById(principal.getId());
        
        boolean connected = user != null && user.getGoogleAccessToken() != null;
        logger.info("Google Status Check for user {}: connected={}", principal.getUsername(), connected);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("connected", connected);
        
        return ResponseEntity.ok(response);
    }

    /**
     * OAuth callback - exchange code for tokens.
     */
    @GetMapping("/callback")
    public void handleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            jakarta.servlet.http.HttpServletResponse response) throws IOException { 
        
        logger.info("Google OAuth callback received. State (UserID): {}", state);
        try {
            if (state == null) {
                logger.error("Callback missing state parameter");
                throw new IOException("Missing state parameter");
            }

            Credential credential = googleCalendarService.exchangeCode(
                    code, googleCalendarConfig.getRedirectUri(), state);
            
            logger.info("Successfully authenticated with Google Calendar for User ID: {}", state);
            
            response.sendRedirect(frontendUrl + "/?google_connected=true");
            
        } catch (Exception e) {
            logger.error("Critical error during Google OAuth callback", e);
            response.sendRedirect(frontendUrl + "/?google_connected=false&error=" + 
                java.net.URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Unknown error", java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /**
     * Fetch events from Google Calendar.
     */
    @GetMapping("/events")
    public ResponseEntity<?> fetchEvents(org.springframework.security.core.Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).build();
            }
            
            com.calendar.model.User user = (com.calendar.model.User) authentication.getPrincipal();
            Long userId = user.getId();
            
            Credential credential = googleCalendarService.getCredentialFromDatabase(userId);
            
            if (credential == null) {
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Not connected to Google Calendar. Please authenticate first.");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<Event> events = googleCalendarService.fetchGoogleCalendarEvents(credential, user);
            return ResponseEntity.ok(events);
            
        } catch (IOException e) {
            logger.error("Error fetching Google Calendar events", e);
            if (e.getMessage().contains("401") || e.getMessage().contains("invalid_grant")) {
                 // Token expired or revoked
                 Map<String, String> error = new HashMap<>();
                 error.put("status", "error");
                 error.put("message", "Google Calendar session expired. Please reconnect.");
                 return ResponseEntity.status(401).body(error);
            }
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to fetch events from Google Calendar");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Export event to Google Calendar.
     */
    @PostMapping("/export")
    public ResponseEntity<Map<String, String>> exportEvent(@RequestBody Event event, org.springframework.security.core.Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).build();
            }
            
            com.calendar.model.User user = (com.calendar.model.User) authentication.getPrincipal();
            Long userId = user.getId();
            
            Credential credential = googleCalendarService.getCredentialFromDatabase(userId);
            
            if (credential == null) {
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Not connected to Google Calendar. Please authenticate first.");
                return ResponseEntity.badRequest().body(error);
            }
            
            Event exported = googleCalendarService.exportToGoogleCalendar(credential, event);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("eventId", exported.getId());
            response.put("message", "Event exported to Google Calendar");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            logger.error("Error exporting event to Google Calendar", e);
            if (e.getMessage().contains("401") || e.getMessage().contains("invalid_grant")) {
                 Map<String, String> error = new HashMap<>();
                 error.put("status", "error");
                 error.put("message", "Google Calendar session expired. Please reconnect.");
                 return ResponseEntity.status(401).body(error);
            }
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to export event to Google Calendar");
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Sync events from Google Calendar to local database.
     */
    @PostMapping("/sync")
    public ResponseEntity<?> syncEvents(org.springframework.security.core.Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).build();
            }
            
            com.calendar.model.User principal = (com.calendar.model.User) authentication.getPrincipal();
            // Reload user
            com.calendar.model.User user = googleCalendarService.getUserById(principal.getId());
            
            logger.info("Initiating two-way sync for user: {}", user.getUsername());
            
            // 1. Pull events from Google to local
            List<com.calendar.model.Event> pulled = googleCalendarService.syncGoogleEvents(user);
            
            // 2. Push local events to Google
            int pushed = googleCalendarService.pushLocalEventsToGoogle(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("pulledCount", pulled.size());
            response.put("pushedCount", pushed);
            response.put("message", String.format("Sync complete! Pulled %d from Google and pushed %d from local.", 
                    pulled.size(), pushed));
            
            logger.info("Completed two-way sync for user {}. Pulled: {}, Pushed: {}", 
                    user.getUsername(), pulled.size(), pushed);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Critical error during two-way Google Calendar sync", e);
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Sync failed: " + e.getMessage());
            error.put("details", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(error);
        }
    }
}
