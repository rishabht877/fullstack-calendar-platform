package com.calendar.repository;

import com.calendar.model.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    List<Calendar> findByUserId(Long userId);
    Optional<Calendar> findByIdAndUserId(Long id, Long userId);
}
