package com.calendar.service;

import com.calendar.dto.EventDTO;
import com.calendar.dto.RecurrenceDTO;
import com.calendar.model.Calendar;
import com.calendar.model.Event;
import com.calendar.repository.CalendarRepository;
import com.calendar.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    public List<EventDTO> getEvents(Long calendarId) {
        return eventRepository.findByCalendarId(calendarId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EventDTO> getEventsInRange(Long calendarId, LocalDateTime start, LocalDateTime end) {
        return eventRepository.findEventsInRange(calendarId, start, end).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "analytics", allEntries = true) // Invalidate analytics when events change
    public EventDTO createEvent(Long calendarId, EventDTO eventDTO) {
        if (eventDTO.getStartTime() != null && eventDTO.getEndTime() != null && eventDTO.getStartTime().isAfter(eventDTO.getEndTime())) {
            throw new RuntimeException("End time cannot be before start time.");
        }

        Calendar calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new RuntimeException("Calendar not found"));

        if (eventDTO.getRecurrence() != null) {
            return createRecurringEvents(calendar, eventDTO);
        }

        // Basic conflict detection
        if (hasConflict(calendarId, eventDTO.getStartTime(), eventDTO.getEndTime())) {
            throw new RuntimeException("Event conflict detected! Overlaps with existing event.");
        }

        Event event = new Event(eventDTO.getSubject(), eventDTO.getStartTime(), eventDTO.getEndTime(), calendar);
        event.setDescription(eventDTO.getDescription());
        event.setLocation(eventDTO.getLocation());
        event.setStatus(eventDTO.getStatus() != null ? eventDTO.getStatus() : "CONFIRMED");
        event.setSeriesId(eventDTO.getSeriesId());

        Event savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
    }

    private EventDTO createRecurringEvents(Calendar calendar, EventDTO eventDTO) {
        RecurrenceDTO recurrence = eventDTO.getRecurrence();
        if (recurrence == null || recurrence.getPattern() == null) {
            throw new RuntimeException("Recurrence pattern is required for recurring events.");
        }

        String seriesId = UUID.randomUUID().toString();
        List<Event> eventsToSave = new ArrayList<>();

        LocalDateTime currentStart = eventDTO.getStartTime();
        LocalDateTime currentEnd = eventDTO.getEndTime();
        java.time.Duration duration = java.time.Duration.between(currentStart, currentEnd);

        int count = 0;
        int maxOccurrences = 1000; // safety limit
        if ("COUNT".equalsIgnoreCase(recurrence.getType())) {
            maxOccurrences = recurrence.getOccurrences() != null ? recurrence.getOccurrences() : 10;
        }
        
        LocalDate untilDate = "DATE".equalsIgnoreCase(recurrence.getType()) ? recurrence.getUntilDate() : null;

        boolean isWeekly = "WEEKLY".equalsIgnoreCase(recurrence.getPattern());
        List<String> targetDays = recurrence.getDaysOfWeek(); 

        LocalDateTime iterator = currentStart;
        int safetyLimit = 3650; // 10 years
        int loopCount = 0;

        while (count < maxOccurrences && loopCount < safetyLimit) {
            if (untilDate != null && iterator.toLocalDate().isAfter(untilDate)) {
                break;
            }

            boolean shouldCreate = false;

            if (isWeekly && targetDays != null && !targetDays.isEmpty()) {
                String currentDayOfWeek = iterator.getDayOfWeek().name();
                if (targetDays.stream().anyMatch(d -> d.equalsIgnoreCase(currentDayOfWeek))) {
                     shouldCreate = true;
                }
            } else {
                shouldCreate = true;
            }

            if (shouldCreate) {
                Event event = new Event(eventDTO.getSubject(), iterator, iterator.plus(duration), calendar);
                event.setDescription(eventDTO.getDescription());
                event.setLocation(eventDTO.getLocation());
                event.setStatus("CONFIRMED");
                event.setSeriesId(seriesId);
                eventsToSave.add(event);
                count++;
            }

            // Advance iterator
            if (isWeekly && targetDays != null && !targetDays.isEmpty()) {
                iterator = iterator.plusDays(1);
            } else {
                 int interval = recurrence.getInterval() != null && recurrence.getInterval() > 0 ? recurrence.getInterval() : 1;
                 String pattern = recurrence.getPattern().toUpperCase();
                 switch (pattern) {
                     case "DAILY":
                         iterator = iterator.plusDays(interval);
                         break;
                     case "WEEKLY":
                         iterator = iterator.plusWeeks(interval);
                         break;
                     case "MONTHLY":
                         iterator = iterator.plusMonths(interval);
                         break;
                     case "YEARLY":
                         iterator = iterator.plusYears(interval);
                         break;
                     default:
                         iterator = iterator.plusDays(1);
                 }
            }
            loopCount++;
        }

        if (eventsToSave.isEmpty()) {
             throw new RuntimeException("No events were created based on the recurrence rules.");
        }

        List<Event> savedEvents = eventRepository.saveAll(eventsToSave);
        return convertToDTO(savedEvents.get(0));
    }

    @CacheEvict(value = "analytics", allEntries = true)
    public EventDTO updateEvent(Long eventId, EventDTO eventDTO) {
        if (eventDTO.getStartTime() != null && eventDTO.getEndTime() != null && eventDTO.getStartTime().isAfter(eventDTO.getEndTime())) {
            throw new RuntimeException("End time cannot be before start time.");
        }
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.setSubject(eventDTO.getSubject());
        event.setStartTime(eventDTO.getStartTime());
        event.setEndTime(eventDTO.getEndTime());
        event.setDescription(eventDTO.getDescription());
        event.setLocation(eventDTO.getLocation());
        event.setStatus(eventDTO.getStatus());
        // Series ID usually doesn't change on single update

        Event updatedEvent = eventRepository.save(event);
        return convertToDTO(updatedEvent);
    }

    @CacheEvict(value = "analytics", allEntries = true)
    public void deleteEvent(Long eventId) {
        eventRepository.deleteById(eventId);
    }

    private boolean hasConflict(Long calendarId, LocalDateTime start, LocalDateTime end) {
        List<Event> conflicts = eventRepository.findEventsInRange(calendarId, start, end);
        // Filter out strict boundary matches if desired, but for now simple overlap
        return !conflicts.isEmpty();
    }

    private EventDTO convertToDTO(Event event) {
        return EventDTO.builder()
                .id(event.getId())
                .subject(event.getSubject())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .description(event.getDescription())
                .location(event.getLocation())
                .status(event.getStatus())
                .seriesId(event.getSeriesId())
                .calendarId(event.getCalendar().getId())
                .build();
    }
}
