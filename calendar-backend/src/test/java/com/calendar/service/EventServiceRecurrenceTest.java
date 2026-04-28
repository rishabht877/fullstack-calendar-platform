package com.calendar.service;

import com.calendar.dto.EventDTO;
import com.calendar.dto.RecurrenceDTO;
import com.calendar.model.Calendar;
import com.calendar.model.Event;
import com.calendar.repository.CalendarRepository;
import com.calendar.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceRecurrenceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @InjectMocks
    private EventService eventService;

    private Calendar mockCalendar;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    public void setUp() {
        mockCalendar = new Calendar();
        mockCalendar.setId(1L);
        start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0); // Tomorrow 10:00
        end = start.plusHours(1); // Tomorrow 11:00
    }

    @Test
    public void createRecurringEvents_DailyPattern_CreatesMultipleEvents() {
        RecurrenceDTO recurrence = new RecurrenceDTO();
        recurrence.setPattern("DAILY");
        recurrence.setInterval(1);
        recurrence.setType("DATE"); // Fix NPE
        recurrence.setUntilDate(LocalDate.from(start.plusDays(4))); // 5 occurrences total (start day + 4 days)

        EventDTO dto = new EventDTO();
        dto.setSubject("Daily Meeting");
        dto.setStartTime(start);
        dto.setEndTime(end);
        dto.setRecurrence(recurrence);

        when(calendarRepository.findById(1L)).thenReturn(Optional.of(mockCalendar));
        // when(eventRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        eventService.createEvent(1L, dto);
        
        // Verify saveAll is called with a list of 5 events
        verify(eventRepository, times(1)).saveAll(argThat(list -> ((List)list).size() == 5));
    }
    
    // Let's rewrite the test to be more robust about the method signature
    // Implementation details: createEvent usually returns a single EventDTO.
    // If it's recurring, does it return the series?
    // Let's verify via the mock calls first.

    @Test
    public void verifySaveAllCalledWithCorrectCount_Daily() {
        RecurrenceDTO recurrence = new RecurrenceDTO();
        recurrence.setPattern("DAILY");
        recurrence.setInterval(1);
        recurrence.setType("DATE"); // Fix NPE
        recurrence.setUntilDate(LocalDate.from(start.plusDays(2))); // 3 days total: T, T+1, T+2

        EventDTO dto = EventDTO.builder()
                .subject("Daily Standup")
                .startTime(start)
                .endTime(end)
                .recurrence(recurrence)
                .build();

        when(calendarRepository.findById(1L)).thenReturn(Optional.of(mockCalendar));
        
        eventService.createEvent(1L, dto);

        verify(eventRepository, times(1)).saveAll(argThat(list -> ((List)list).size() == 3));
    }

    @Test
    public void createRecurringEvents_WeeklyPattern_CreatesWeeklyEvents() {
        RecurrenceDTO recurrence = new RecurrenceDTO();
        recurrence.setPattern("WEEKLY");
        recurrence.setInterval(1);
        recurrence.setType("DATE"); // Fix NPE
        recurrence.setUntilDate(LocalDate.from(start.plusWeeks(2))); // 3 weeks: T, T+1wk, T+2wk

        EventDTO dto = EventDTO.builder()
                .subject("Weekly Sync")
                .startTime(start)
                .endTime(end)
                .recurrence(recurrence)
                .build();

        when(calendarRepository.findById(1L)).thenReturn(Optional.of(mockCalendar));

        eventService.createEvent(1L, dto);

        verify(eventRepository, times(1)).saveAll(argThat(list -> ((List)list).size() == 3));
    }
}
