package com.calendar.repository;

import com.calendar.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    // Find events in a specific calendar
    List<Event> findByCalendarId(Long calendarId);

    // Find events between dates for a specific calendar
    @Query("SELECT e FROM Event e WHERE e.calendar.id = :calendarId AND " +
           "((e.startTime BETWEEN :start AND :end) OR " +
           "(e.endTime BETWEEN :start AND :end) OR " +
           "(e.startTime <= :start AND e.endTime >= :end))")
    List<Event> findEventsInRange(@Param("calendarId") Long calendarId, 
                                  @Param("start") LocalDateTime start, 
                                  @Param("end") LocalDateTime end);

    // Find events in a series
    List<Event> findBySeriesId(String seriesId);

    // Count future events for user (for analytics)
    @Query("SELECT COUNT(e) FROM Event e WHERE e.calendar.user.id = :userId AND e.startTime > CURRENT_TIMESTAMP")
    long countUpcomingEvents(@Param("userId") Long userId);
    
    // Count events by user in date range
    @Query("SELECT COUNT(e) FROM Event e WHERE e.calendar.user.id = :userId AND e.startTime BETWEEN :start AND :end")
    long countEventsInTimeRange(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Find all events for a user (for in-memory analytics)
    List<Event> findByCalendar_User_Id(Long userId);
    // Find event by Google ID for sync duplicate prevention
    java.util.Optional<Event> findByGoogleEventId(String googleEventId);
}
