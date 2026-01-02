package com.calendar.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    private User user;
    private Calendar calendar;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "password");
        user.setId(1L);
        calendar = new Calendar("Test Calendar", "UTC", user);
        calendar.setId(1L);
        startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        endTime = LocalDateTime.of(2024, 1, 1, 11, 0);
    }

    @Test
    void constructor_CreatesEventWithRequiredFields() {
        Event event = new Event("Meeting", startTime, endTime, calendar);
        
        assertEquals("Meeting", event.getSubject());
        assertEquals(startTime, event.getStartTime());
        assertEquals(endTime, event.getEndTime());
        assertEquals(calendar, event.getCalendar());
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        Event event = new Event();
        
        event.setId(100L);
        event.setSubject("Test Event");
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setDescription("Test Description");
        event.setLocation("Test Location");
        event.setStatus("CONFIRMED");
        event.setSeriesId("series-123");
        event.setCalendar(calendar);
        
        assertEquals(100L, event.getId());
        assertEquals("Test Event", event.getSubject());
        assertEquals(startTime, event.getStartTime());
        assertEquals(endTime, event.getEndTime());
        assertEquals("Test Description", event.getDescription());
        assertEquals("Test Location", event.getLocation());
        assertEquals("CONFIRMED", event.getStatus());
        assertEquals("series-123", event.getSeriesId());
        assertEquals(calendar, event.getCalendar());
    }

    @Test
    void createdAt_IsSetAutomatically() {
        Event event = new Event("Meeting", startTime, endTime, calendar);
        assertNotNull(event.getCreatedAt());
    }

    @Test
    void updatedAt_IsSetAutomatically() {
        Event event = new Event("Meeting", startTime, endTime, calendar);
        assertNotNull(event.getUpdatedAt());
    }
}
