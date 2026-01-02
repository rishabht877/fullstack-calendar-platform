package com.calendar.service;

import com.calendar.dto.AnalyticsDTO;
import com.calendar.repository.CalendarRepository;
import com.calendar.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Cacheable(value = "analytics", key = "#userId")
    public AnalyticsDTO getUserAnalytics(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDateTime monthStart = now.withDayOfMonth(1);

        // Fetch all events for detailed analysis
        var allEvents = eventRepository.findByCalendar_User_Id(userId);
        
        long totalEvents = allEvents.size();
        long weekEvents = allEvents.stream().filter(e -> e.getStartTime().isAfter(weekStart) && e.getStartTime().isBefore(weekStart.plusDays(7))).count();
        long monthEvents = allEvents.stream().filter(e -> e.getStartTime().isAfter(monthStart) && e.getStartTime().isBefore(monthStart.plusMonths(1))).count();
        long upcomingEvents = allEvents.stream().filter(e -> e.getStartTime().isAfter(now)).count();
        long totalCalendars = calendarRepository.findByUserId(userId).size();

        // Calculate average events per day (simplified over last 30 days for relevance)
        // Or simply Total / 365 if we want year average. Let's keep it simple: Total / (Active Days) roughly
        // For now, let's keep the previous valid logic or improve it.
        // Let's use events in last 30 days / 30.
        long last30DaysCount = allEvents.stream().filter(e -> e.getStartTime().isAfter(now.minusDays(30))).count();
        double averageEventsPerDay = last30DaysCount / 30.0;

        // Group by Status
        Map<String, Long> statusBreakdown = new HashMap<>();
        allEvents.forEach(e -> statusBreakdown.merge(e.getStatus() != null ? e.getStatus() : "CONFIRMED", 1L, Long::sum));

        // Group by Subject (Top 5)
        Map<String, Long> eventsBySubject = new HashMap<>();
        allEvents.forEach(e -> eventsBySubject.merge(e.getSubject(), 1L, Long::sum));
        // Sort and limit/filtering can be done here or frontend. Map is unordered.

        // Group by Weekday
        Map<String, Long> eventsByWeekday = new HashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            eventsByWeekday.put(day.name(), 0L);
        }
        allEvents.forEach(e -> {
            String day = e.getStartTime().getDayOfWeek().name();
            eventsByWeekday.merge(day, 1L, Long::sum);
        });

        // Online Percentage
        long onlineCount = allEvents.stream()
                .filter(e -> e.getLocation() != null && 
                       (e.getLocation().toLowerCase().contains("zoom") || 
                        e.getLocation().toLowerCase().contains("meet") || 
                        e.getLocation().toLowerCase().contains("online") ||
                        e.getLocation().toLowerCase().contains("teams")))
                .count();
        double onlinePercentage = totalEvents > 0 ? (double) onlineCount / totalEvents * 100 : 0;

        // Busiest and Least Busy Days
        String busiestDayOfWeek = eventsByWeekday.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("MONDAY");
        String leastBusyDayOfWeek = eventsByWeekday.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("SUNDAY");

        return AnalyticsDTO.builder()
                .totalEvents(totalEvents)
                .weekEvents(weekEvents)
                .monthEvents(monthEvents)
                .upcomingEvents(upcomingEvents)
                .totalCalendars(totalCalendars)
                .averageEventsPerDay(averageEventsPerDay)
                .busiestDayOfWeek(busiestDayOfWeek)
                .leastBusyDayOfWeek(leastBusyDayOfWeek)
                .eventsByStatus(statusBreakdown)
                .eventsBySubject(eventsBySubject)
                .eventsByWeekday(eventsByWeekday)
                .onlinePercentage(onlinePercentage)
                .build();
    }
}
