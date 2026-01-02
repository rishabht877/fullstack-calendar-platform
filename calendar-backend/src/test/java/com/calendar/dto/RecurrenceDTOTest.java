package com.calendar.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class RecurrenceDTOTest {

    @Test
    void constructor_CreatesRecurrenceDTOWithAllFields() {
        RecurrenceDTO dto = new RecurrenceDTO(
                "WEEKLY",
                2,
                Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY"),
                "COUNT",
                10,
                LocalDate.now().plusMonths(3)
        );
        
        assertEquals("WEEKLY", dto.getPattern());
        assertEquals(2, dto.getInterval());
        assertEquals(3, dto.getDaysOfWeek().size());
        assertEquals("COUNT", dto.getType());
        assertEquals(10, dto.getOccurrences());
        assertNotNull(dto.getUntilDate());
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        RecurrenceDTO dto = new RecurrenceDTO();
        
        dto.setPattern("DAILY");
        dto.setInterval(1);
        dto.setDaysOfWeek(Arrays.asList("MONDAY"));
        dto.setType("DATE");
        dto.setOccurrences(5);
        dto.setUntilDate(LocalDate.now().plusWeeks(2));
        
        assertEquals("DAILY", dto.getPattern());
        assertEquals(1, dto.getInterval());
        assertEquals(1, dto.getDaysOfWeek().size());
        assertEquals("DATE", dto.getType());
        assertEquals(5, dto.getOccurrences());
        assertNotNull(dto.getUntilDate());
    }
}
