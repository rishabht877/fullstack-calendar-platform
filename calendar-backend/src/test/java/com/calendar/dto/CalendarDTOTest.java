package com.calendar.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalendarDTOTest {

    @Test
    void builder_CreatesCalendarDTOWithAllFields() {
        CalendarDTO dto = CalendarDTO.builder()
                .id(1L)
                .name("Work")
                .timezone("America/New_York")
                .color("#FF0000")
                .userId(10L)
                .build();
        
        assertEquals(1L, dto.getId());
        assertEquals("Work", dto.getName());
        assertEquals("America/New_York", dto.getTimezone());
        assertEquals("#FF0000", dto.getColor());
        assertEquals(10L, dto.getUserId());
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        CalendarDTO dto = new CalendarDTO();
        
        dto.setId(2L);
        dto.setName("Personal");
        dto.setTimezone("UTC");
        dto.setColor("#00FF00");
        dto.setUserId(20L);
        
        assertEquals(2L, dto.getId());
        assertEquals("Personal", dto.getName());
        assertEquals("UTC", dto.getTimezone());
        assertEquals("#00FF00", dto.getColor());
        assertEquals(20L, dto.getUserId());
    }
}
