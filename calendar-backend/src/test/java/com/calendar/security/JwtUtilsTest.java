package com.calendar.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private String testSecret = "testSecretKeyMustBeLongEnoughToSatisfyHMACSHA256Requirements";
    private int testExpirationMs = 3600000; // 1 hour

    @BeforeEach
    public void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", testExpirationMs);
    }

    @Test
    public void generateJwtToken_Success() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = jwtUtils.generateJwtToken(authentication);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void getUserNameFromJwtToken_ValidToken_ReturnsUsername() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String token = jwtUtils.generateJwtToken(authentication);

        String username = jwtUtils.getUserNameFromJwtToken(token);

        assertEquals("testUser", username);
    }

    @Test
    public void validateJwtToken_ValidToken_ReturnsTrue() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String token = jwtUtils.generateJwtToken(authentication);

        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    public void validateJwtToken_InvalidSignature_ReturnsFalse() {
        // Create a token with a different secret
        String diffSecret = "anotherSecretKeyForTestingInvalidSignatures123456";
        String token = Jwts.builder()
                .setSubject("testUser")
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + testExpirationMs))
                .signWith(Keys.hmacShaKeyFor(diffSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    public void validateJwtToken_MalformedToken_ReturnsFalse() {
        assertFalse(jwtUtils.validateJwtToken("invalid.token.string"));
    }

    @Test
    public void validateJwtToken_ExpiredToken_ReturnsFalse() {
        // Generate an expired token manually
        String token = Jwts.builder()
                .setSubject("testUser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 20000)) // 20s ago
                .setExpiration(new Date(System.currentTimeMillis() - 10000)) // Expired 10s ago
                .signWith(Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    public void validateJwtToken_EmptyToken_ReturnsFalse() {
        assertFalse(jwtUtils.validateJwtToken(""));
        assertFalse(jwtUtils.validateJwtToken(null));
    }
}
