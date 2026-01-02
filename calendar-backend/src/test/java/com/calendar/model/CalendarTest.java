package com.calendar.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalendarTest {

    @Test
    void constructor_CreatesCalendarWithRequiredFields() {
        User user = new User("testuser", "test@example.com", "password");
        Calendar calendar = new Calendar("Work", "America/New_York", user);
        
        assertEquals("Work", calendar.getName());
        assertEquals("America/New_York", calendar.getTimezone());
        assertEquals(user, calendar.getUser());
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        User user = new User("testuser", "test@example.com", "password");
        Calendar calendar = new Calendar();
        
        calendar.setId(1L);
        calendar.setName("Personal");
        calendar.setTimezone("UTC");
        calendar.setColor("#FF0000");
        calendar.setUser(user);
        
        assertEquals(1L, calendar.getId());
        assertEquals("Personal", calendar.getName());
        assertEquals("UTC", calendar.getTimezone());
        assertEquals("#FF0000", calendar.getColor());
        assertEquals(user, calendar.getUser());
    }

    @Test
    void createdAt_IsSetAutomatically() {
        User user = new User("testuser", "test@example.com", "password");
        Calendar calendar = new Calendar("Work", "UTC", user);
        assertNotNull(calendar.getCreatedAt());
    }

    @Test
    void updatedAt_IsSetAutomatically() {
        User user = new User("testuser", "test@example.com", "password");
        Calendar calendar = new Calendar("Work", "UTC", user);
        assertNotNull(calendar.getUpdatedAt());
    }
}
