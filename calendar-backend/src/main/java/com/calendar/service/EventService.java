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
        String seriesId = UUID.randomUUID().toString();
        List<Event> eventsToSave = new ArrayList<>();

        LocalDateTime currentStart = eventDTO.getStartTime();
        LocalDateTime currentEnd = eventDTO.getEndTime();
        
        // Duration of the event
        java.time.Duration duration = java.time.Duration.between(currentStart, currentEnd);

        int count = 0;
        int maxOccurrences = recurrence.getType().equals("COUNT") ? recurrence.getOccurrences() : 1000; // safety limit
        LocalDate untilDate = recurrence.getType().equals("DATE") ? recurrence.getUntilDate() : null;

        // If it's weekly and specific days are selected, we basically need to advance day by day 
        // until we hit a matching day, then add event, then continue.
        // OR we can jump by weeks.
        
        // Correct approach for "Weekly with multiple days (e.g., MWF)":
        // 1. Start from the event start date.
        // 2. Iterate day by day (or optimize).
        // 3. Check if current day matches one of the selected days.
        // 4. If yes, add event.
        // 5. Stop when count reached or date passed.

        boolean isWeekly = "WEEKLY".equalsIgnoreCase(recurrence.getPattern());
        List<String> targetDays = recurrence.getDaysOfWeek(); 
        // Normalize target days to uppercase just in case: MONDAY, TUESDAY...

        LocalDateTime iterator = currentStart;

        // Safety loop limit to prevent infinite
        int safetyLimit = 365 * 5; 
        int loopCount = 0;

        while (count < maxOccurrences && loopCount < safetyLimit) {
            if (untilDate != null && iterator.toLocalDate().isAfter(untilDate)) {
                break;
            }

            boolean shouldCreate = false;

            if (isWeekly && targetDays != null && !targetDays.isEmpty()) {
                // Check if current day is in targetDays
                String currentDayOfWeek = iterator.getDayOfWeek().name(); // MONDAY, TUESDAY...
                // Only consider checking if we match the day
                // But we must also consider the "Interval".
                // Complication: strict interval logic with specific days. 
                // Usually "Every 2 weeks on MWF" means:
                // Week 1: Mon, Wed, Fri (if date >= start)
                // Week 2: Skip
                // Week 3: Mon, Wed, Fri
                
                // For simplicity MVP: Assumes Interval = 1 or ignores interval for specific days logic 
                // unless we implement full week-tracking.
                // Let's assume Interval=1 for now if not specified.
                
                // For the user request "MWF repeated 5 times":
                // If we are just daily iterating, we check if day matches.
                
                // Optimization: to support skipping days.
                if (targetDays.stream().anyMatch(d -> d.equalsIgnoreCase(currentDayOfWeek))) {
                     shouldCreate = true;
                }
            } else {
                // Daily, Monthly, or Weekly without specific days (just 7 days later)
                shouldCreate = true;
            }

            if (shouldCreate) {
                // Check conflict? Maybe skip or warn. For now, we allowed strict conflicts valid, 
                // or we can fail the whole batch. Let's skip conflict check for batch for now or log it.
                
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
                // Move to next day
                iterator = iterator.plusDays(1);
            } else {
                // Apply standard interval
                 int interval = recurrence.getInterval() != null && recurrence.getInterval() > 0 ? recurrence.getInterval() : 1;
                 switch (recurrence.getPattern().toUpperCase()) {
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
                         iterator = iterator.plusDays(1); // fallback
                 }
            }
            loopCount++;
        }

        List<Event> savedEvents = eventRepository.saveAll(eventsToSave);
        return !savedEvents.isEmpty() ? convertToDTO(savedEvents.get(0)) : null;
    }

    @CacheEvict(value = "analytics", allEntries = true)
    public EventDTO updateEvent(Long eventId, EventDTO eventDTO) {
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
