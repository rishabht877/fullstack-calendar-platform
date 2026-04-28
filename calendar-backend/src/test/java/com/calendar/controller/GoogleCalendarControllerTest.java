package com.calendar.controller;

import com.calendar.config.GoogleCalendarConfig;
import com.calendar.service.GoogleCalendarService;
import com.google.api.services.calendar.model.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoogleCalendarController.class)
@org.springframework.context.annotation.Import(com.calendar.config.SecurityConfig.class)
public class GoogleCalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoogleCalendarService googleCalendarService;

    @MockBean
    private GoogleCalendarConfig googleCalendarConfig;

    @MockBean
    private com.calendar.security.JwtUtils jwtUtils;

    @MockBean
    private com.calendar.security.UserDetailsServiceImpl userDetailsService;

    @MockBean
    private com.calendar.config.AuthEntryPointJwt authEntryPointJwt;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // Ensure frontend redirect URI is mocked to prevent NPE
        when(googleCalendarConfig.getFrontendRedirectUri()).thenReturn("http://localhost:5173");
        when(googleCalendarConfig.getRedirectUri()).thenReturn("http://localhost:8080/api/google/callback");
        
        // Setup Security Context manually to avoid ClassCastException
        com.calendar.model.User customUser = new com.calendar.model.User("testuser", "test@example.com", "password");
        customUser.setId(1L);
        
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(customUser, null, customUser.getAuthorities());
            
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void initiateAuth_ReturnsAuthUrl() throws Exception {
        String authUrl = "https://accounts.google.com/o/oauth2/auth";
        when(googleCalendarService.getAuthorizationUrl(any(), any())).thenReturn(authUrl);
        // Correct endpoint is /api/google/auth
        mockMvc.perform(get("/api/google/auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authUrl").value(authUrl));
    }

    @Test
    public void getStatus_Connected_ReturnsTrue() throws Exception {
        // Mock service returning user with token
        com.calendar.model.User userWithToken = new com.calendar.model.User();
        userWithToken.setGoogleAccessToken("valid-token");
        when(googleCalendarService.getUserById(1L)).thenReturn(userWithToken);

        mockMvc.perform(get("/api/google/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true));
    }

    @Test
    public void getStatus_NotConnected_ReturnsFalse() throws Exception {
         // Mock service returning user without token
        com.calendar.model.User userNoToken = new com.calendar.model.User();
        when(googleCalendarService.getUserById(1L)).thenReturn(userNoToken);

        mockMvc.perform(get("/api/google/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false));
    }

    @Test
    public void handleCallback_Success() throws Exception {
        // Clear context for this test as usage is unauthenticated (or authenticated via token in param?)
        // Controller handleCallback doesn't use Principal. It uses 'state' param as userId.
        // But Security Config permits it.
        // Manual context setup in setUp() might persist?
        // It's fine if it's there, but strictly speaking callback comes from Google.
        
        String code = "auth_code";
        String redirectUri = "http://localhost:5173";
        
        // Controller calls exchangeCode directly
        when(googleCalendarService.exchangeCode(anyString(), anyString(), anyString())).thenReturn(null);

        mockMvc.perform(get("/api/google/callback")
                .param("code", code)
                .param("state", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(redirectUri + "/?google_connected=true"));
        
        // Verify exchangeCode called with correct args
        // exchangeCode(code, redirectUri, userId)
        verify(googleCalendarService, times(1)).exchangeCode(eq(code), anyString(), eq("1"));
    }

    @Test
    public void fetchEvents_Success() throws Exception {
        Event event = new Event().setSummary("Test Event");
        List<Event> events = Collections.singletonList(event);
        
        // Controller calls getCredentialFromDatabase
        when(googleCalendarService.getCredentialFromDatabase(1L)).thenReturn(mock(com.google.api.client.auth.oauth2.Credential.class));
        when(googleCalendarService.fetchGoogleCalendarEvents(any(), any())).thenReturn(events);

        mockMvc.perform(get("/api/google/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].summary").value("Test Event"));
    }

    @Test
    public void fetchEvents_AuthError() throws Exception {
        when(googleCalendarService.getCredentialFromDatabase(1L)).thenReturn(mock(com.google.api.client.auth.oauth2.Credential.class));
        when(googleCalendarService.fetchGoogleCalendarEvents(any(), any())).thenThrow(new java.io.IOException("401 Unauthorized"));

        mockMvc.perform(get("/api/google/events"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Google Calendar session expired. Please reconnect."));
    }

    @Test
    public void fetchEvents_GenericError() throws Exception {
        when(googleCalendarService.getCredentialFromDatabase(1L)).thenReturn(mock(com.google.api.client.auth.oauth2.Credential.class));
        when(googleCalendarService.fetchGoogleCalendarEvents(any(), any())).thenThrow(new java.io.IOException("Generic Error"));

        mockMvc.perform(get("/api/google/events"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to fetch events from Google Calendar"));
    }

    @Test
    public void exportEvent_Success() throws Exception {
        // Mock credential
        when(googleCalendarService.getCredentialFromDatabase(1L)).thenReturn(mock(com.google.api.client.auth.oauth2.Credential.class));
        
        Event exportedEvent = new Event().setId("evt123").setSummary("Test Event");
        when(googleCalendarService.exportToGoogleCalendar(any(), any(Event.class))).thenReturn(exportedEvent);

        mockMvc.perform(post("/api/google/export")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"summary\":\"Test Event\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    public void syncEvents_Success() throws Exception {
        // Mock sync
        com.calendar.model.User mockUser = new com.calendar.model.User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        when(googleCalendarService.getUserById(1L)).thenReturn(mockUser);
        
        when(googleCalendarService.syncGoogleEvents(any())).thenReturn(Collections.emptyList());
        when(googleCalendarService.pushLocalEventsToGoogle(any())).thenReturn(0);

        mockMvc.perform(post("/api/google/sync"))
                .andExpect(status().isOk());
    }

    @Test
    public void syncEvents_Exception() throws Exception {
        // Mock sync exception
        com.calendar.model.User mockUser = new com.calendar.model.User();
        mockUser.setId(1L);
        when(googleCalendarService.getUserById(1L)).thenReturn(mockUser);
        
        when(googleCalendarService.syncGoogleEvents(any())).thenThrow(new RuntimeException("Sync Error"));

        mockMvc.perform(post("/api/google/sync"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Sync failed: Sync Error"));
    }
}
