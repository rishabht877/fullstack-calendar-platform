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

class AnalyticsServiceExtendedTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

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

    @Test
    void getUserAnalytics_WithOnlineEvents() {
        Event event1 = new Event("Zoom Meeting", LocalDateTime.now(), LocalDateTime.now().plusHours(1), testCalendar);
        event1.setLocation("https://zoom.us/j/123456");
        
        Event event2 = new Event("Google Meet", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1), testCalendar);
        event2.setLocation("https://meet.google.com/abc-def-ghi");
        
        Event event3 = new Event("In Person", LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(1), testCalendar);
        event3.setLocation("Office");
        
        when(eventRepository.findByCalendar_User_Id(1L)).thenReturn(Arrays.asList(event1, event2, event3));
        when(calendarRepository.findByUserId(1L)).thenReturn(List.of(testCalendar));
        
        AnalyticsDTO result = analyticsService.getUserAnalytics(1L);
        
        assertNotNull(result);
        assertEquals(3, result.getTotalEvents());
        assertTrue(result.getOnlinePercentage() > 0);
    }

    @Test
    void getUserAnalytics_WithTeamsEvents() {
        Event event = new Event("Teams Meeting", LocalDateTime.now(), LocalDateTime.now().plusHours(1), testCalendar);
        event.setLocation("Microsoft Teams");
        
        when(eventRepository.findByCalendar_User_Id(1L)).thenReturn(List.of(event));
        when(calendarRepository.findByUserId(1L)).thenReturn(List.of(testCalendar));
        
        AnalyticsDTO result = analyticsService.getUserAnalytics(1L);
        
        assertTrue(result.getOnlinePercentage() > 0);
    }

    @Test
    void getUserAnalytics_WithDifferentStatuses() {
        Event event1 = new Event("Confirmed", LocalDateTime.now(), LocalDateTime.now().plusHours(1), testCalendar);
        event1.setStatus("CONFIRMED");
        
        Event event2 = new Event("Tentative", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1), testCalendar);
        event2.setStatus("TENTATIVE");
        
        Event event3 = new Event("Cancelled", LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(1), testCalendar);
        event3.setStatus("CANCELLED");
        
        when(eventRepository.findByCalendar_User_Id(1L)).thenReturn(Arrays.asList(event1, event2, event3));
        when(calendarRepository.findByUserId(1L)).thenReturn(List.of(testCalendar));
        
        AnalyticsDTO result = analyticsService.getUserAnalytics(1L);
        
        assertNotNull(result.getEventsByStatus());
        assertTrue(result.getEventsByStatus().containsKey("CONFIRMED"));
        assertTrue(result.getEventsByStatus().containsKey("TENTATIVE"));
    }

    @Test
    void getUserAnalytics_WeekdayDistribution() {
        LocalDateTime monday = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY);
        LocalDateTime tuesday = monday.plusDays(1);
        LocalDateTime wednesday = monday.plusDays(2);
        
        Event event1 = new Event("Monday Event", monday, monday.plusHours(1), testCalendar);
        Event event2 = new Event("Tuesday Event", tuesday, tuesday.plusHours(1), testCalendar);
        Event event3 = new Event("Wednesday Event", wednesday, wednesday.plusHours(1), testCalendar);
        
        when(eventRepository.findByCalendar_User_Id(1L)).thenReturn(Arrays.asList(event1, event2, event3));
        when(calendarRepository.findByUserId(1L)).thenReturn(List.of(testCalendar));
        
        AnalyticsDTO result = analyticsService.getUserAnalytics(1L);
        
        assertNotNull(result.getEventsByWeekday());
        assertTrue(result.getEventsByWeekday().containsKey("MONDAY"));
        assertEquals(1L, result.getEventsByWeekday().get("MONDAY"));
    }

    @Test
    void getUserAnalytics_BusiestAndLeastBusyDays() {
        LocalDateTime monday = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY);
        
        Event event1 = new Event("Event 1", monday, monday.plusHours(1), testCalendar);
        Event event2 = new Event("Event 2", monday.plusHours(2), monday.plusHours(3), testCalendar);
        Event event3 = new Event("Event 3", monday.plusHours(4), monday.plusHours(5), testCalendar);
        
        when(eventRepository.findByCalendar_User_Id(1L)).thenReturn(Arrays.asList(event1, event2, event3));
        when(calendarRepository.findByUserId(1L)).thenReturn(List.of(testCalendar));
        
        AnalyticsDTO result = analyticsService.getUserAnalytics(1L);
        
        assertNotNull(result.getBusiestDayOfWeek());
        assertNotNull(result.getLeastBusyDayOfWeek());
    }

    @Test
    void getUserAnalytics_AverageEventsPerDay() {
        LocalDateTime now = LocalDateTime.now();
        
        Event event1 = new Event("Recent 1", now.minusDays(5), now.minusDays(5).plusHours(1), testCalendar);
        Event event2 = new Event("Recent 2", now.minusDays(10), now.minusDays(10).plusHours(1), testCalendar);
        Event event3 = new Event("Recent 3", now.minusDays(15), now.minusDays(15).plusHours(1), testCalendar);
        
        when(eventRepository.findByCalendar_User_Id(1L)).thenReturn(Arrays.asList(event1, event2, event3));
        when(calendarRepository.findByUserId(1L)).thenReturn(List.of(testCalendar));
        
        AnalyticsDTO result = analyticsService.getUserAnalytics(1L);
        
        assertTrue(result.getAverageEventsPerDay() >= 0);
    }

    @Test
    void getUserAnalytics_UpcomingEvents() {
        LocalDateTime future = LocalDateTime.now().plusDays(5);
        
        Event event1 = new Event("Future Event", future, future.plusHours(1), testCalendar);
        Event event2 = new Event("Past Event", LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(5).plusHours(1), testCalendar);
        
        when(eventRepository.findByCalendar_User_Id(1L)).thenReturn(Arrays.asList(event1, event2));
        when(calendarRepository.findByUserId(1L)).thenReturn(List.of(testCalendar));
        
        AnalyticsDTO result = analyticsService.getUserAnalytics(1L);
        
        assertEquals(1, result.getUpcomingEvents());
    }
}
