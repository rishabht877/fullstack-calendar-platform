package com.calendar.controller;

import com.calendar.dto.EventDTO;
import com.calendar.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping("/calendar/{calendarId}")
    public List<EventDTO> getEvents(@PathVariable Long calendarId) {
        return eventService.getEvents(calendarId);
    }

    @GetMapping("/calendar/{calendarId}/range")
    public List<EventDTO> getEventsInRange(
            @PathVariable Long calendarId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return eventService.getEventsInRange(calendarId, start, end);
    }

    @PostMapping("/calendar/{calendarId}")
    public EventDTO createEvent(@PathVariable Long calendarId, @RequestBody EventDTO eventDTO) {
        return eventService.createEvent(calendarId, eventDTO);
    }

    @PutMapping("/{id}")
    public EventDTO updateEvent(@PathVariable Long id, @RequestBody EventDTO eventDTO) {
        return eventService.updateEvent(id, eventDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok().build();
    }
}
