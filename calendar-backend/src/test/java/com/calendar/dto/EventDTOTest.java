package com.calendar.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventDTOTest {

    @Test
    void builder_CreatesEventDTOWithAllFields() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        
        EventDTO dto = EventDTO.builder()
                .id(1L)
                .subject("Meeting")
                .startTime(start)
                .endTime(end)
                .description("Important meeting")
                .location("Office")
                .status("CONFIRMED")
                .seriesId("series-1")
                .calendarId(10L)
                .build();
        
        assertEquals(1L, dto.getId());
        assertEquals("Meeting", dto.getSubject());
        assertEquals(start, dto.getStartTime());
        assertEquals(end, dto.getEndTime());
        assertEquals("Important meeting", dto.getDescription());
        assertEquals("Office", dto.getLocation());
        assertEquals("CONFIRMED", dto.getStatus());
        assertEquals("series-1", dto.getSeriesId());
        assertEquals(10L, dto.getCalendarId());
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        EventDTO dto = new EventDTO();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        
        dto.setId(2L);
        dto.setSubject("Conference");
        dto.setStartTime(start);
        dto.setEndTime(end);
        dto.setDescription("Annual conference");
        dto.setLocation("Convention Center");
        dto.setStatus("TENTATIVE");
        dto.setSeriesId("series-2");
        dto.setCalendarId(20L);
        
        assertEquals(2L, dto.getId());
        assertEquals("Conference", dto.getSubject());
        assertEquals(start, dto.getStartTime());
        assertEquals(end, dto.getEndTime());
        assertEquals("Annual conference", dto.getDescription());
        assertEquals("Convention Center", dto.getLocation());
        assertEquals("TENTATIVE", dto.getStatus());
        assertEquals("series-2", dto.getSeriesId());
        assertEquals(20L, dto.getCalendarId());
    }
}
