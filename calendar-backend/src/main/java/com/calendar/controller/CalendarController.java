package com.calendar.controller;

import com.calendar.model.Calendar;
import com.calendar.repository.UserRepository;
import com.calendar.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendars")
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername()).get().getId();
    }

    @GetMapping
    public List<com.calendar.dto.CalendarDTO> getUserCalendars() {
        return calendarService.getUserCalendars(getCurrentUserId());
    }

    @PostMapping
    public com.calendar.dto.CalendarDTO createCalendar(@RequestBody Map<String, String> payload) {
        return calendarService.createCalendar(getCurrentUserId(), payload.get("name"), payload.get("timezone"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCalendar(@PathVariable Long id) {
        calendarService.deleteCalendar(id, getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportCalendar(@PathVariable Long id) {
        byte[] icsData = calendarService.exportCalendar(id, getCurrentUserId());
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"calendar.ics\"")
                .header("Content-Type", "text/calendar")
                .body(icsData);
    }
}
