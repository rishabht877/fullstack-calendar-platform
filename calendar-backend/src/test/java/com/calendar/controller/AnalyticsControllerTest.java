package com.calendar.controller;

import com.calendar.dto.AnalyticsDTO;
import com.calendar.model.User;
import com.calendar.repository.UserRepository;
import com.calendar.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "testuser")
    void getAnalytics_Success() throws Exception {
        // Arrange
        User mockUser = new User("testuser", "test@example.com", "password");
        mockUser.setId(1L);

        AnalyticsDTO analyticsDTO = AnalyticsDTO.builder()
                .totalEvents(10)
                .eventsBySubject(new HashMap<>())
                .eventsByWeekday(new HashMap<>())
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(analyticsService.getUserAnalytics(anyLong())).thenReturn(analyticsDTO);

        // Act & Assert
        mockMvc.perform(get("/api/analytics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").value(10));
    }
}
