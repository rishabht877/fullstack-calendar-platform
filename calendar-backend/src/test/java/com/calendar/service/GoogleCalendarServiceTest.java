package com.calendar.service;

import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.repository.CalendarRepository;
import com.calendar.repository.EventRepository;
import com.calendar.repository.UserRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoogleCalendarServiceTest {

    @Mock
    private GoogleAuthorizationCodeFlow flow;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private JsonFactory jsonFactory;

    @Spy
    @InjectMocks
    private GoogleCalendarService googleCalendarService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setGoogleAccessToken("access-token");
        user.setGoogleRefreshToken("refresh-token");
    }

    @Test
    void exchangeCode_Success() throws IOException {
        String code = "auth-code";
        String redirectUri = "http://localhost:8080/callback";
        String userIdState = "1";

        GoogleTokenResponse tokenResponse = new GoogleTokenResponse();
        tokenResponse.setAccessToken("new-access-token");
        tokenResponse.setRefreshToken("new-refresh-token");
        tokenResponse.setExpiresInSeconds(3600L);

        GoogleAuthorizationCodeTokenRequest tokenRequest = mock(GoogleAuthorizationCodeTokenRequest.class);
        when(flow.newTokenRequest(code)).thenReturn(tokenRequest);
        when(tokenRequest.setRedirectUri(redirectUri)).thenReturn(tokenRequest);
        when(tokenRequest.execute()).thenReturn(tokenResponse);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flow.createAndStoreCredential(tokenResponse, userIdState)).thenReturn(mock(Credential.class));

        Credential result = googleCalendarService.exchangeCode(code, redirectUri, userIdState);

        assertNotNull(result);
        verify(userRepository).save(user);
        assertEquals("new-access-token", user.getGoogleAccessToken());
    }

    @Test
    void getCredentialFromDatabase_Success() throws IOException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flow.createAndStoreCredential(any(TokenResponse.class), eq("1"))).thenReturn(mock(Credential.class));

        Credential credential = googleCalendarService.getCredentialFromDatabase(1L);
        assertNotNull(credential);
    }
    
    @Test
    void getCredentialFromDatabase_NoToken() {
        user.setGoogleAccessToken(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        Credential credential = googleCalendarService.getCredentialFromDatabase(1L);
        assertNull(credential);
    }

    @Test
    void syncGoogleEvents_Success() throws IOException {
        // Mock getCredential (internal call)
        doReturn(mock(Credential.class)).when(googleCalendarService).getCredentialFromDatabase(1L);
        
        // Mock fetchGoogleCalendarEvents (internal call)
        com.google.api.services.calendar.model.Event googleEvent = new com.google.api.services.calendar.model.Event();
        googleEvent.setId("g123");
        googleEvent.setSummary("Google Event");
        doReturn(Collections.singletonList(googleEvent)).when(googleCalendarService).fetchGoogleCalendarEvents(any(), any());

        // Mock repositories
        when(calendarRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(calendarRepository.save(any())).thenReturn(new com.calendar.model.Calendar("Google Calendar", "UTC", user));
        when(eventRepository.findByGoogleEventId("g123")).thenReturn(Optional.empty());
        when(eventRepository.save(any())).thenReturn(new Event());

        var result = googleCalendarService.syncGoogleEvents(user);

        assertEquals(1, result.size());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void pushLocalEventsToGoogle_Success() throws IOException {
        doReturn(mock(Credential.class)).when(googleCalendarService).getCredentialFromDatabase(1L);
        
        Event localEvent = new Event();
        localEvent.setId(10L);
        localEvent.setSubject("Local Event");
        localEvent.setStartTime(LocalDateTime.now());
        localEvent.setEndTime(LocalDateTime.now().plusHours(1));

        when(eventRepository.findByCalendar_User_Id(1L)).thenReturn(Collections.singletonList(localEvent));
        
        // Mock exportToGoogleCalendar
        doReturn(new com.google.api.services.calendar.model.Event().setId("g999"))
            .when(googleCalendarService).exportToGoogleCalendar(any(), any());

        int pushed = googleCalendarService.pushLocalEventsToGoogle(user);

        assertEquals(1, pushed);
        verify(eventRepository).save(localEvent);
        assertEquals("g999", localEvent.getGoogleEventId());
    }
}
