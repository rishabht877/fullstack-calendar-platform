package com.calendar.service;

import com.calendar.dto.AnalyticsDTO;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyticsServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private User testUser;
    private Calendar testCalendar;
    private List<Event> testEvents;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);
        
        testCalendar = new Calendar("Test Calendar", "UTC", testUser);
        testCalendar.setId(1L);
        
        // Create test events
        Event event1 = new Event("Meeting", LocalDateTime.now(), LocalDateTime.now().plusHours(1), testCalendar);
        event1.setLocation("Office");
        
        Event event2 = new Event("Online Meeting", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1), testCalendar);
        event2.setLocation("https://zoom.us/meeting");
        
        Event event3 = new Event("Conference", LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(2), testCalendar);
        event3.setLocation("Conference Room");
        
        testEvents = Arrays.asList(event1, event2, event3);
    }

    @Test
    void getAnalytics_Success() {
        // Arrange
        when(eventRepository.findByCalendar_User_Id(1L)).thenReturn(testEvents);
        when(calendarRepository.findByUserId(1L)).thenReturn(List.of(testCalendar));

        // Act
        AnalyticsDTO result = analyticsService.getUserAnalytics(1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalEvents());
        assertNotNull(result.getEventsBySubject());
        assertNotNull(result.getEventsByWeekday());
        verify(eventRepository, times(1)).findByCalendar_User_Id(1L);
    }

    @Test
    void getAnalytics_NoEvents() {
        // Arrange
        when(eventRepository.findByCalendar_User_Id(1L)).thenReturn(List.of());
        when(calendarRepository.findByUserId(1L)).thenReturn(List.of(testCalendar));

        // Act
        AnalyticsDTO result = analyticsService.getUserAnalytics(1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalEvents());
    }
}
