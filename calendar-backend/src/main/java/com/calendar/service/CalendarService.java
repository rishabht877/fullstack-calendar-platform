package com.calendar.service;

import com.calendar.model.Calendar;
import com.calendar.model.User;
import com.calendar.repository.CalendarRepository;
import com.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarService {

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.calendar.repository.EventRepository eventRepository;

    public List<com.calendar.dto.CalendarDTO> getUserCalendars(Long userId) {
        return calendarRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public com.calendar.dto.CalendarDTO createCalendar(Long userId, String name, String timezone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Calendar calendar = new Calendar(name, timezone, user);
        Calendar saved = calendarRepository.save(calendar);
        return convertToDTO(saved);
    }

    public void deleteCalendar(Long calendarId, Long userId) {
        Calendar calendar = calendarRepository.findByIdAndUserId(calendarId, userId)
                .orElseThrow(() -> new RuntimeException("Calendar not found or access denied"));
        calendarRepository.delete(calendar);
    }

    public byte[] exportCalendar(Long calendarId, Long userId) {
        // Verify access
        Calendar calendar = calendarRepository.findByIdAndUserId(calendarId, userId)
                .orElseThrow(() -> new RuntimeException("Calendar not found or access denied"));

        List<com.calendar.model.Event> events = eventRepository.findByCalendarId(calendarId);
        
        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\n");
        ics.append("VERSION:2.0\n");
        ics.append("PRODID:-//My Calendar App//EN\n");
        
        if (calendar.getTimezone() != null) {
            ics.append("X-WR-TIMEZONE:").append(calendar.getTimezone()).append("\n");
        }

        for (com.calendar.model.Event event : events) {
            ics.append("BEGIN:VEVENT\n");
            ics.append("UID:").append(event.getId()).append("@calendarapp.com\n");
            ics.append("SUMMARY:").append(escapeIcs(event.getSubject())).append("\n");
            ics.append("DTSTART:").append(formatDate(event.getStartTime())).append("\n");
            ics.append("DTEND:").append(formatDate(event.getEndTime())).append("\n");
            if (event.getDescription() != null) {
                ics.append("DESCRIPTION:").append(escapeIcs(event.getDescription())).append("\n");
            }
            if (event.getLocation() != null) {
                ics.append("LOCATION:").append(escapeIcs(event.getLocation())).append("\n");
            }
            ics.append("END:VEVENT\n");
        }

        ics.append("END:VCALENDAR\n");
        return ics.toString().getBytes();
    }

    private String escapeIcs(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n");
    }

    private String formatDate(java.time.LocalDateTime ldt) {
        // Simple format: YYYYMMDDTHHMMSS
        return ldt.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
    }

    private com.calendar.dto.CalendarDTO convertToDTO(Calendar calendar) {
        return com.calendar.dto.CalendarDTO.builder()
                .id(calendar.getId())
                .name(calendar.getName())
                .timezone(calendar.getTimezone())
                .color(calendar.getColor())
                .userId(calendar.getUser().getId())
                .build();
    }
}
