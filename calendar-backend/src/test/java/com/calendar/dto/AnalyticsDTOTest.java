package com.calendar.dto;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsDTOTest {

    @Test
    void builder_CreatesAnalyticsDTOWithAllFields() {
        Map<String, Long> eventsBySubject = new HashMap<>();
        eventsBySubject.put("Meeting", 5L);
        
        Map<String, Long> eventsByWeekday = new HashMap<>();
        eventsByWeekday.put("MONDAY", 3L);
        
        Map<String, Long> eventsByStatus = new HashMap<>();
        eventsByStatus.put("CONFIRMED", 10L);
        
        AnalyticsDTO dto = AnalyticsDTO.builder()
                .totalEvents(15L)
                .weekEvents(3L)
                .monthEvents(10L)
                .upcomingEvents(5L)
                .totalCalendars(2L)
                .averageEventsPerDay(0.5)
                .busiestDayOfWeek("MONDAY")
                .leastBusyDayOfWeek("SUNDAY")
                .eventsByStatus(eventsByStatus)
                .eventsBySubject(eventsBySubject)
                .eventsByWeekday(eventsByWeekday)
                .onlinePercentage(60.0)
                .build();
        
        assertEquals(15L, dto.getTotalEvents());
        assertEquals(3L, dto.getWeekEvents());
        assertEquals(10L, dto.getMonthEvents());
        assertEquals(5L, dto.getUpcomingEvents());
        assertEquals(2L, dto.getTotalCalendars());
        assertEquals(0.5, dto.getAverageEventsPerDay());
        assertEquals("MONDAY", dto.getBusiestDayOfWeek());
        assertEquals("SUNDAY", dto.getLeastBusyDayOfWeek());
        assertEquals(60.0, dto.getOnlinePercentage());
        assertNotNull(dto.getEventsBySubject());
        assertNotNull(dto.getEventsByWeekday());
        assertNotNull(dto.getEventsByStatus());
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        AnalyticsDTO dto = new AnalyticsDTO();
        
        dto.setTotalEvents(20L);
        dto.setWeekEvents(5L);
        dto.setMonthEvents(15L);
        dto.setUpcomingEvents(8L);
        dto.setTotalCalendars(3L);
        dto.setAverageEventsPerDay(0.75);
        dto.setBusiestDayOfWeek("FRIDAY");
        dto.setLeastBusyDayOfWeek("SATURDAY");
        dto.setOnlinePercentage(75.0);
        
        assertEquals(20L, dto.getTotalEvents());
        assertEquals(5L, dto.getWeekEvents());
        assertEquals(15L, dto.getMonthEvents());
        assertEquals(8L, dto.getUpcomingEvents());
        assertEquals(3L, dto.getTotalCalendars());
        assertEquals(0.75, dto.getAverageEventsPerDay());
        assertEquals("FRIDAY", dto.getBusiestDayOfWeek());
        assertEquals("SATURDAY", dto.getLeastBusyDayOfWeek());
        assertEquals(75.0, dto.getOnlinePercentage());
    }
}
