package com.calendar.service;

import com.calendar.dto.EventDTO;
import com.calendar.dto.RecurrenceDTO;
import com.calendar.model.Calendar;
import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.repository.CalendarRepository;
import com.calendar.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class EventServiceExtendedTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @InjectMocks
    private EventService eventService;

    private User testUser;
    private Calendar testCalendar;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);
        
        testCalendar = new Calendar("Test Calendar", "UTC", testUser);
        testCalendar.setId(1L);
    }

    private RecurrenceDTO createRecurrence(String pattern, int interval, String type, Integer occurrences, LocalDate untilDate) {
        RecurrenceDTO dto = new RecurrenceDTO();
        dto.setPattern(pattern);
        dto.setInterval(interval);
        dto.setType(type);
        dto.setOccurrences(occurrences);
        dto.setUntilDate(untilDate);
        return dto;
    }

    @Test
    void updateEvent_Success() {
        Event existingEvent = new Event("Old Subject", LocalDateTime.now(), LocalDateTime.now().plusHours(1), testCalendar);
        existingEvent.setId(1L);
        
        EventDTO updateDTO = EventDTO.builder()
                .subject("New Subject")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .description("Updated description")
                .location("New location")
                .status("TENTATIVE")
                .build();
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(existingEvent);
        
        EventDTO result = eventService.updateEvent(1L, updateDTO);
        
        assertNotNull(result);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void updateEvent_NotFound_ThrowsException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());
        
        EventDTO updateDTO = EventDTO.builder()
                .subject("Test")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .build();
        
        assertThrows(RuntimeException.class, () -> eventService.updateEvent(1L, updateDTO));
    }

    @Test
    void deleteEvent_Success() {
        eventService.deleteEvent(1L);
        verify(eventRepository, times(1)).deleteById(1L);
    }

    @Test
    void getEventsInRange_Success() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(7);
        
        Event event1 = new Event("Event 1", start.plusDays(1), start.plusDays(1).plusHours(1), testCalendar);
        Event event2 = new Event("Event 2", start.plusDays(2), start.plusDays(2).plusHours(1), testCalendar);
        
        when(eventRepository.findEventsInRange(1L, start, end)).thenReturn(Arrays.asList(event1, event2));
        
        List<EventDTO> result = eventService.getEventsInRange(1L, start, end);
        
        assertEquals(2, result.size());
    }

    @Test
    void createEvent_WithAllProperties() {
        EventDTO eventDTO = EventDTO.builder()
                .subject("Complete Event")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .description("Full description")
                .location("Conference Room A")
                .status("CONFIRMED")
                .build();
        
        when(calendarRepository.findById(1L)).thenReturn(Optional.of(testCalendar));
        when(eventRepository.findEventsInRange(anyLong(), any(), any())).thenReturn(List.of());
        
        Event savedEvent = new Event("Complete Event", eventDTO.getStartTime(), eventDTO.getEndTime(), testCalendar);
        savedEvent.setId(1L);
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
        
        EventDTO result = eventService.createEvent(1L, eventDTO);
        
        assertNotNull(result);
        assertEquals("Complete Event", result.getSubject());
    }

    @Test
    void createRecurringEvent_Daily() {
        EventDTO eventDTO = EventDTO.builder()
                .subject("Daily Standup")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(30))
                .recurrence(createRecurrence("DAILY", 1, "COUNT", 5, null))
                .build();
        
        when(calendarRepository.findById(1L)).thenReturn(Optional.of(testCalendar));
        
        Event savedEvent = new Event("Daily Standup", eventDTO.getStartTime(), eventDTO.getEndTime(), testCalendar);
        savedEvent.setId(1L);
        when(eventRepository.saveAll(any())).thenReturn(List.of(savedEvent));
        
        EventDTO result = eventService.createEvent(1L, eventDTO);
        
        verify(eventRepository, times(1)).saveAll(any());
    }

    @Test
    void createRecurringEvent_Monthly() {
        EventDTO eventDTO = EventDTO.builder()
                .subject("Monthly Review")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .recurrence(createRecurrence("MONTHLY", 1, "COUNT", 3, null))
                .build();
        
        when(calendarRepository.findById(1L)).thenReturn(Optional.of(testCalendar));
        
        Event savedEvent = new Event("Monthly Review", eventDTO.getStartTime(), eventDTO.getEndTime(), testCalendar);
        savedEvent.setId(2L);
        when(eventRepository.saveAll(any())).thenReturn(List.of(savedEvent));
        
        EventDTO result = eventService.createEvent(1L, eventDTO);
        
        verify(eventRepository, times(1)).saveAll(any());
    }

    @Test
    void createRecurringEvent_WithUntilDate() {
        EventDTO eventDTO = EventDTO.builder()
                .subject("Weekly Meeting")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .recurrence(createRecurrence("WEEKLY", 1, "DATE", null, LocalDate.now().plusWeeks(4)))
                .build();
        
        when(calendarRepository.findById(1L)).thenReturn(Optional.of(testCalendar));
        
        Event savedEvent = new Event("Weekly Meeting", eventDTO.getStartTime(), eventDTO.getEndTime(), testCalendar);
        savedEvent.setId(3L);
        when(eventRepository.saveAll(any())).thenReturn(List.of(savedEvent));
        
        EventDTO result = eventService.createEvent(1L, eventDTO);
        
        verify(eventRepository, times(1)).saveAll(any());
    }

    @Test
    void createEvent_CalendarNotFound_ThrowsException() {
        when(calendarRepository.findById(1L)).thenReturn(Optional.empty());
        
        EventDTO eventDTO = EventDTO.builder()
                .subject("Test")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .build();
        
        assertThrows(RuntimeException.class, () -> eventService.createEvent(1L, eventDTO));
    }
}
