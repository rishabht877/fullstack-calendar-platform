package com.calendar.controller;

import com.calendar.dto.EventDTO;
import com.calendar.model.User;
import com.calendar.repository.UserRepository;
import com.calendar.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "testuser")
    void getEvents_Success() throws Exception {
        // Arrange
        User mockUser = new User("testuser", "test@example.com", "password");
        mockUser.setId(1L);

        EventDTO eventDTO = EventDTO.builder()
                .id(1L)
                .subject("Test Event")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(eventService.getEvents(anyLong())).thenReturn(Collections.singletonList(eventDTO));

        // Act & Assert
        mockMvc.perform(get("/api/events/calendar/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subject").value("Test Event"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createEvent_Success() throws Exception {
        // Arrange
        User mockUser = new User("testuser", "test@example.com", "password");
        mockUser.setId(1L);

        EventDTO inputDTO = EventDTO.builder()
                .subject("New Event")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .description("Test Description")
                .build();

        EventDTO savedDTO = EventDTO.builder()
                .id(1L)
                .subject("New Event")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(eventService.createEvent(anyLong(), any(EventDTO.class))).thenReturn(savedDTO);

        // Act & Assert
        mockMvc.perform(post("/api/events/calendar/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("New Event"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteEvent_Success() throws Exception {
        // Arrange
        User mockUser = new User("testuser", "test@example.com", "password");
        mockUser.setId(1L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // Act & Assert
        mockMvc.perform(delete("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
