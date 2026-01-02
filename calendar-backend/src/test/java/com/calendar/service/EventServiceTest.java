package com.calendar.service;

import com.calendar.dto.EventDTO;
import com.calendar.model.Calendar;
import com.calendar.model.Event;
import com.calendar.repository.CalendarRepository;
import com.calendar.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEvent_Success() {
        // Arrange
        Long calendarId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);
        
        EventDTO inputDTO = EventDTO.builder()
                .subject("Test Event")
                .startTime(start)
                .endTime(end)
                .description("Test Description")
                .build();

        Calendar mockCalendar = new Calendar();
        mockCalendar.setId(calendarId);

        Event savedEvent = new Event("Test Event", start, end, mockCalendar);
        savedEvent.setId(100L);
        savedEvent.setDescription("Test Description");
        savedEvent.setStatus("CONFIRMED");

        when(calendarRepository.findById(calendarId)).thenReturn(Optional.of(mockCalendar));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        // Act
        EventDTO result = eventService.createEvent(calendarId, inputDTO);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Test Event", result.getSubject());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void createEvent_Conflict() {
        // Arrange
        Long calendarId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);
        
        EventDTO inputDTO = EventDTO.builder()
                .subject("Conflict Event")
                .startTime(start)
                .endTime(end)
                .build();

        Calendar mockCalendar = new Calendar();
        
        when(calendarRepository.findById(calendarId)).thenReturn(Optional.of(mockCalendar));
        // Simulate conflict finding existing events
        when(eventRepository.findEventsInRange(any(), any(), any())).thenReturn(java.util.List.of(new Event()));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> eventService.createEvent(calendarId, inputDTO));
    }
}
