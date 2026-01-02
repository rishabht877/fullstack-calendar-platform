package com.calendar.service;

import com.calendar.dto.CalendarDTO;
import com.calendar.model.Calendar;
import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.repository.CalendarRepository;
import com.calendar.repository.EventRepository;
import com.calendar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CalendarServiceExtendedTest {

    @Mock
    private CalendarRepository calendarRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CalendarService calendarService;

    private User testUser;
    private Calendar testCalendar;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);
        
        testCalendar = new Calendar("Test Calendar", "UTC", testUser);
        testCalendar.setId(1L);
    }

    @Test
    void deleteCalendar_Success() {
        when(calendarRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCalendar));
        
        calendarService.deleteCalendar(1L, 1L);
        
        verify(calendarRepository, times(1)).delete(testCalendar);
    }

    @Test
    void deleteCalendar_NotFound_ThrowsException() {
        when(calendarRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> calendarService.deleteCalendar(1L, 1L));
    }

    @Test
    void exportCalendar_Success() {
        Event event1 = new Event("Meeting", LocalDateTime.now(), LocalDateTime.now().plusHours(1), testCalendar);
        event1.setId(1L);
        event1.setDescription("Test meeting");
        event1.setLocation("Office");
        
        when(calendarRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCalendar));
        when(eventRepository.findByCalendarId(1L)).thenReturn(Arrays.asList(event1));
        
        byte[] result = calendarService.exportCalendar(1L, 1L);
        
        assertNotNull(result);
        String icsContent = new String(result);
        assertTrue(icsContent.contains("BEGIN:VCALENDAR"));
        assertTrue(icsContent.contains("END:VCALENDAR"));
        assertTrue(icsContent.contains("BEGIN:VEVENT"));
        assertTrue(icsContent.contains("SUMMARY:Meeting"));
    }

    @Test
    void exportCalendar_WithSpecialCharacters() {
        Event event = new Event("Meeting; with, special\\chars", LocalDateTime.now(), LocalDateTime.now().plusHours(1), testCalendar);
        event.setDescription("Description\nwith\nnewlines");
        event.setLocation("Location;with;semicolons");
        
        when(calendarRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testCalendar));
        when(eventRepository.findByCalendarId(1L)).thenReturn(Arrays.asList(event));
        
        byte[] result = calendarService.exportCalendar(1L, 1L);
        
        assertNotNull(result);
        String icsContent = new String(result);
        assertTrue(icsContent.contains("\\;"));
        assertTrue(icsContent.contains("\\n"));
    }

    @Test
    void exportCalendar_NotFound_ThrowsException() {
        when(calendarRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> calendarService.exportCalendar(1L, 1L));
    }

    @Test
    void getUserCalendars_MultipleCalendars() {
        Calendar cal1 = new Calendar("Work", "UTC", testUser);
        cal1.setId(1L);
        Calendar cal2 = new Calendar("Personal", "America/New_York", testUser);
        cal2.setId(2L);
        cal2.setColor("#FF0000");
        
        when(calendarRepository.findByUserId(1L)).thenReturn(Arrays.asList(cal1, cal2));
        
        List<CalendarDTO> result = calendarService.getUserCalendars(1L);
        
        assertEquals(2, result.size());
        assertEquals("Work", result.get(0).getName());
        assertEquals("Personal", result.get(1).getName());
        assertEquals("#FF0000", result.get(1).getColor());
    }

    @Test
    void createCalendar_WithColor() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        Calendar savedCalendar = new Calendar("Colorful", "UTC", testUser);
        savedCalendar.setId(5L);
        savedCalendar.setColor("#00FF00");
        
        when(calendarRepository.save(any(Calendar.class))).thenReturn(savedCalendar);
        
        CalendarDTO result = calendarService.createCalendar(1L, "Colorful", "UTC");
        
        assertEquals(5L, result.getId());
        assertEquals("Colorful", result.getName());
    }

    @Test
    void createCalendar_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> calendarService.createCalendar(1L, "Test", "UTC"));
    }
}
