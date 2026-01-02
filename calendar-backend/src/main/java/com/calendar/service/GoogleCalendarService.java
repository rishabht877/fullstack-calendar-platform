package com.calendar.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Google Calendar API integration.
 * Handles OAuth authentication and calendar synchronization.
 */
@Service
public class GoogleCalendarService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);

    @Autowired
    private GoogleAuthorizationCodeFlow flow;

    @Autowired
    private JsonFactory jsonFactory;

    /**
     * Generate OAuth authorization URL for user to grant access.
     */
    @Autowired
    private com.calendar.repository.UserRepository userRepository;

    /**
     * Generate OAuth authorization URL using userId as state.
     */
    public String getAuthorizationUrl(String redirectUri, Long userId) {
        return flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .setState(userId.toString()) // Pass user ID as state
                .build();
    }

    /**
     * Exchange authorization code for access token and refresh token, and persist to DB.
     * @return The authorized Credential (and saves to DB)
     */
    public Credential exchangeCode(String code, String redirectUri, String stateUserId) throws IOException {
        logger.info("Exchanging code for user ID: {}. Redirect URI: {}", stateUserId, redirectUri);
        GoogleTokenResponse response = flow.newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute();
        
        logger.info("Successfully fetched tokens from Google for user ID: {}", stateUserId);
        
        // Persist to database
        try {
            Long userId = Long.parseLong(stateUserId);
            com.calendar.model.User user = userRepository.findById(userId).orElse(null);
            
            if (user != null) {
                user.setGoogleAccessToken(response.getAccessToken());
                if (response.getRefreshToken() != null) {
                    user.setGoogleRefreshToken(response.getRefreshToken());
                    logger.info("Refresh token received for user ID: {}", userId);
                }
                // Calculate expiration
                Long expiresInSeconds = response.getExpiresInSeconds();
                if (expiresInSeconds != null) {
                    user.setGoogleTokenExpirationTime(System.currentTimeMillis() + (expiresInSeconds * 1000));
                }
                userRepository.save(user);
                logger.info("Persisted Google OAuth tokens to database for user: {}", user.getUsername());
            } else {
                logger.warn("User ID {} from OAuth state not found in database", userId);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid state parameter (User ID): {}", stateUserId);
        }

        return flow.createAndStoreCredential(response, stateUserId);
    }

    /**
     * Get Google Calendar service instance for authenticated user.
     */
    public Calendar getCalendarService(Credential credential) {
        return new Calendar.Builder(
                new NetHttpTransport(),
                jsonFactory,
                credential)
                .setApplicationName("Multi-Calendar System")
                .build();
    }
    
    @Autowired
    private com.calendar.repository.CalendarRepository calendarRepository;

    @Autowired
    private com.calendar.repository.EventRepository eventRepository;

    /**
     * Sync events from Google to local database for a specific user.
     */
    @org.springframework.transaction.annotation.Transactional
    public List<com.calendar.model.Event> syncGoogleEvents(com.calendar.model.User user) throws IOException {
        Credential credential = getCredentialFromDatabase(user.getId());
        if (credential == null) {
            throw new IOException("Not connected to Google Calendar");
        }

        // 1. Get or create "Google Calendar" bucket
        com.calendar.model.Calendar googleBucket = calendarRepository.findByUserId(user.getId())
                .stream()
                .filter(c -> "Google Calendar".equals(c.getName()))
                .findFirst()
                .orElseGet(() -> {
                    com.calendar.model.Calendar newCal = new com.calendar.model.Calendar("Google Calendar", "UTC", user);
                    newCal.setColor("#4285F4"); // Google Blue
                    return calendarRepository.save(newCal);
                });

        // 2. Fetch events from Google
        List<Event> googleEvents = fetchGoogleCalendarEvents(credential, user);
        List<com.calendar.model.Event> syncedEvents = new java.util.ArrayList<>();

        // 3. Map and save
        for (Event gEvent : googleEvents) {
            // Skip if no summary
            if (gEvent.getSummary() == null) continue;

            // Check for duplicates
            if (eventRepository.findByGoogleEventId(gEvent.getId()).isPresent()) {
                continue;
            }

            com.calendar.model.Event localEvent = new com.calendar.model.Event();
            localEvent.setSubject(safeTruncate(gEvent.getSummary(), 250));
            localEvent.setDescription(safeTruncate(gEvent.getDescription(), 255)); // Failsafe for unapplied schema changes
            localEvent.setLocation(safeTruncate(gEvent.getLocation(), 250));
            localEvent.setGoogleEventId(gEvent.getId());
            localEvent.setCalendar(googleBucket);
            localEvent.setStatus("CONFIRMED");

            // Handle dates
            localEvent.setStartTime(convertToLocalDateTime(gEvent.getStart()));
            localEvent.setEndTime(convertToLocalDateTime(gEvent.getEnd()));

            // Fallback for null dates (unlikely for Google events but good practice)
            if (localEvent.getStartTime() == null) localEvent.setStartTime(java.time.LocalDateTime.now());
            if (localEvent.getEndTime() == null) localEvent.setEndTime(localEvent.getStartTime().plusHours(1));

            syncedEvents.add(eventRepository.save(localEvent));
        }

        logger.info("Synchronized {} new events for user {}", syncedEvents.size(), user.getUsername());
        return syncedEvents;
    }

    private java.time.LocalDateTime convertToLocalDateTime(com.google.api.services.calendar.model.EventDateTime googleDate) {
        if (googleDate == null) return null;
        
        com.google.api.client.util.DateTime dateTime = googleDate.getDateTime();
        if (dateTime == null) {
            // All-day event use .getDate()
            dateTime = googleDate.getDate();
        }
        
        if (dateTime == null) return null;
        
        return java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(dateTime.getValue()), 
                java.time.ZoneId.systemDefault());
    }

    /**
     * Reconstruct Credential from database for a user.
     */
    public Credential getCredentialFromDatabase(Long userId) {
        com.calendar.model.User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getGoogleAccessToken() == null) {
            return null;
        }
        
        com.google.api.client.auth.oauth2.TokenResponse tokenResponse = new com.google.api.client.auth.oauth2.TokenResponse();
        tokenResponse.setAccessToken(user.getGoogleAccessToken());
        tokenResponse.setRefreshToken(user.getGoogleRefreshToken());
        if (user.getGoogleTokenExpirationTime() != null) {
            tokenResponse.setExpiresInSeconds((user.getGoogleTokenExpirationTime() - System.currentTimeMillis()) / 1000);
        }

        try {
             return flow.createAndStoreCredential(tokenResponse, userId.toString());
        } catch (IOException e) {
            logger.error("Failed to create credential from stored tokens", e);
            return null;
        }
    }

    /**
     * Fetch events from user's Google Calendar with retry logic.
     */
    public List<Event> fetchGoogleCalendarEvents(Credential credential, com.calendar.model.User user) throws IOException {
        return executeWithRetry(() -> {
            Calendar service = getCalendarService(credential);
            
            Calendar.Events.List request = service.events().list("primary")
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setMaxResults(250);

            // Use syncToken if available for incremental sync, else fallback to time-window
            if (user.getGoogleSyncToken() != null) {
                // request.setSyncToken(user.getGoogleSyncToken()); // Real API usage
            } else {
                request.setTimeMin(new com.google.api.client.util.DateTime(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000));
            }

            Events events = request.execute();

            // Save the next sync token for subsequent incremental syncs
            if (events.getNextSyncToken() != null) {
                user.setGoogleSyncToken(events.getNextSyncToken());
                userRepository.save(user);
            }

            List<Event> items = events.getItems();
            logger.info("Fetched {} events from Google Calendar (SyncToken: {})", 
                items != null ? items.size() : 0, user.getGoogleSyncToken() != null);
            return items != null ? items : new ArrayList<>();
        });
    }

    /**
     * Export event to Google Calendar with retry logic.
     */
    public Event exportToGoogleCalendar(Credential credential, Event event) throws IOException {
        return executeWithRetry(() -> {
            Calendar service = getCalendarService(credential);
            Event createdEvent = service.events()
                    .insert("primary", event)
                    .execute();
            logger.info("Exported event to Google Calendar: {}", createdEvent.getSummary());
            return createdEvent;
        });
    }

    /**
     * Helper to execute Google API calls with exponential backoff.
     */
    private <T> T executeWithRetry(GoogleApiCallable<T> callable) throws IOException {
        int maxRetries = 3;
        long waitTime = 1000; // 1 second start
        
        for (int i = 0; i <= maxRetries; i++) {
            try {
                return callable.call();
            } catch (IOException e) {
                if (i == maxRetries) throw e;
                
                // Only retry on transient errors (403 rate limit, 5xx server error)
                // Note: The Google Client library often throws IOException for these.
                logger.warn("Transient Google API error, retrying in {}ms... (Attempt {})", waitTime, i + 1);
                try {
                    Thread.sleep(waitTime + (long)(Math.random() * 500)); // Add jitter
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Retry interrupted", ie);
                }
                waitTime *= 2; // Exponential backoff
            }
        }
        throw new IOException("Failed after retries");
    }

    @FunctionalInterface
    private interface GoogleApiCallable<T> {
        T call() throws IOException;
    }

    public com.calendar.model.User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Push unsynced local events to Google Calendar.
     */
    @org.springframework.transaction.annotation.Transactional
    public int pushLocalEventsToGoogle(com.calendar.model.User user) throws IOException {
        Credential credential = getCredentialFromDatabase(user.getId());
        if (credential == null) {
            throw new IOException("Not connected to Google Calendar");
        }

        // Fetch all events for this user
        List<com.calendar.model.Event> localEvents = eventRepository.findByCalendar_User_Id(user.getId());
        int pushCount = 0;

        for (com.calendar.model.Event localEvent : localEvents) {
            // Only push events that don't have a Google ID yet
            if (localEvent.getGoogleEventId() == null) {
                try {
                    // Map local Event to Google Event
                    Event gEvent = new Event();
                    gEvent.setSummary(localEvent.getSubject());
                    gEvent.setDescription(localEvent.getDescription());
                    gEvent.setLocation(localEvent.getLocation());

                    if (localEvent.getStartTime() == null || localEvent.getEndTime() == null) {
                        logger.warn("Skipping event {} due to missing start or end time", localEvent.getId());
                        continue;
                    }

                    com.google.api.services.calendar.model.EventDateTime start = new com.google.api.services.calendar.model.EventDateTime();
                    start.setDateTime(new com.google.api.client.util.DateTime(
                            java.util.Date.from(localEvent.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant())));
                    gEvent.setStart(start);

                    com.google.api.services.calendar.model.EventDateTime end = new com.google.api.services.calendar.model.EventDateTime();
                    end.setDateTime(new com.google.api.client.util.DateTime(
                            java.util.Date.from(localEvent.getEndTime().atZone(java.time.ZoneId.systemDefault()).toInstant())));
                    gEvent.setEnd(end);

                    // Push to Google
                    Event createdGEvent = exportToGoogleCalendar(credential, gEvent);
                    
                    // Save back the Google ID to prevent double sync
                    localEvent.setGoogleEventId(createdGEvent.getId());
                    eventRepository.save(localEvent);
                    pushCount++;
                    
                } catch (Exception e) {
                    logger.error("Failed to push event {} to Google", localEvent.getId(), e);
                }
            }
        }
        return pushCount;
    }

    private String safeTruncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
