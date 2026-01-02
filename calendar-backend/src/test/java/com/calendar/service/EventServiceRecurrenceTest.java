package com.calendar.service;

import com.calendar.dto.EventDTO;
import com.calendar.dto.RecurrenceDTO;
import com.calendar.model.Calendar;
import com.calendar.model.Event;
import com.calendar.repository.CalendarRepository;
import com.calendar.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventServiceRecurrenceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    public void testCreateRecurringEvents_MWF() {
        // Setup
        Long calendarId = 1L;
        LocalDateTime start = LocalDateTime.of(2025, 12, 19, 10, 0); // Friday
        LocalDateTime end = LocalDateTime.of(2025, 12, 19, 11, 0);

        Calendar calendar = new Calendar();
        calendar.setId(calendarId);
        
        when(calendarRepository.findById(calendarId)).thenReturn(Optional.of(calendar));
        when(eventRepository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);

        // Recurrence: MWF, 5 times
        RecurrenceDTO recurrence = new RecurrenceDTO();
        recurrence.setPattern("WEEKLY");
        recurrence.setDaysOfWeek(Arrays.asList("MONDAY", "WEDNESDAY", "FRIDAY"));
        recurrence.setType("COUNT");
        recurrence.setOccurrences(5);
        recurrence.setInterval(1);

        EventDTO eventDTO = new EventDTO();
        eventDTO.setSubject("Gym");
        eventDTO.setStartTime(start);
        eventDTO.setEndTime(end);
        eventDTO.setRecurrence(recurrence);

        // Execute
        eventService.createEvent(calendarId, eventDTO);

        // Verify
        ArgumentCaptor<List<Event>> captor = ArgumentCaptor.forClass(List.class);
        verify(eventRepository).saveAll(captor.capture());

        List<Event> savedEvents = captor.getValue();
        assertEquals(5, savedEvents.size());

        // 1. Fri Dec 19
        assertEquals(LocalDateTime.of(2025, 12, 19, 10, 0), savedEvents.get(0).getStartTime());
        // 2. Mon Dec 22
        assertEquals(LocalDateTime.of(2025, 12, 22, 10, 0), savedEvents.get(1).getStartTime());
        // 3. Wed Dec 24
        assertEquals(LocalDateTime.of(2025, 12, 24, 10, 0), savedEvents.get(2).getStartTime());
        // 4. Fri Dec 26
        assertEquals(LocalDateTime.of(2025, 12, 26, 10, 0), savedEvents.get(3).getStartTime());
        // 5. Mon Dec 29
        assertEquals(LocalDateTime.of(2025, 12, 29, 10, 0), savedEvents.get(4).getStartTime());
    }
}
