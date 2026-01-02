package com.calendar.controller;

import com.calendar.dto.CalendarDTO;
import com.calendar.model.Calendar;
import com.calendar.model.User;
import com.calendar.repository.UserRepository;
import com.calendar.service.CalendarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarService calendarService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "testuser")
    void getUserCalendars_Success() throws Exception {
        // Arrange
        User mockUser = new User("testuser", "password", "test@example.com");
        mockUser.setId(1L);

        CalendarDTO calendarDTO = CalendarDTO.builder()
                .id(10L)
                .name("Test Calendar")
                .timezone("UTC")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(calendarService.getUserCalendars(1L)).thenReturn(Collections.singletonList(calendarDTO));

        // Act & Assert
        mockMvc.perform(get("/api/calendars")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Calendar"))
                .andExpect(jsonPath("$[0].id").value(10));
    }
}
