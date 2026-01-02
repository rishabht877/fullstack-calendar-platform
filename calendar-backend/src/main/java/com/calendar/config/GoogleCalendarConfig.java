package com.calendar.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * Configuration for Google Calendar API OAuth 2.0 integration.
 */
@Configuration
public class GoogleCalendarConfig {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    @Bean
    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow() {
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
                .setClientId(clientId)
                .setClientSecret(clientSecret);

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets()
                .setWeb(details);

        return new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                clientSecrets,
                SCOPES)
                .setAccessType("offline")
                .build();
    }

    @Bean
    public JsonFactory jsonFactory() {
        return JSON_FACTORY;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
