package com.calendar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.calendar.dto.LoginRequest;
import com.calendar.dto.RegisterRequest;
import com.calendar.model.User;
import com.calendar.repository.UserRepository;
import com.calendar.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void registerUser_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(new User());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void loginUser_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        User userDetails = new User("testuser", "test@example.com", "password");
        userDetails.setId(1L);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("fake-jwt-token");
        
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
