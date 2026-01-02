package com.calendar.service;

import com.calendar.dto.CalendarDTO;
import com.calendar.model.Calendar;
import com.calendar.model.User;
import com.calendar.repository.CalendarRepository;
import com.calendar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CalendarServiceTest {

    @Mock
    private CalendarRepository calendarRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CalendarService calendarService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getUserCalendars_Success() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        
        Calendar cal1 = new Calendar("Work", "UTC", user);
        cal1.setId(10L);
        Calendar cal2 = new Calendar("Home", "UTC", user);
        cal2.setId(11L);

        when(calendarRepository.findByUserId(userId)).thenReturn(List.of(cal1, cal2));

        // Act
        List<CalendarDTO> result = calendarService.getUserCalendars(userId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Work", result.get(0).getName());
    }

    @Test
    void createCalendar_Success() {
        // Arrange
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        user.setId(1L);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        CalendarDTO input = new CalendarDTO();
        input.setName("New Cal");
        input.setTimezone("EST");

        Calendar saved = new Calendar("New Cal", "EST", user);
        saved.setId(5L);

        when(calendarRepository.save(any(Calendar.class))).thenReturn(saved);

        // Act
        // Current signature seems to receive userId or User object? 
        // Let's assume based on previous reads it takes (User user, CalendarDTO dto) or similar? 
        // Wait, the error said "required: Long, String, String".
        // So it's createCalendar(Long userId, String name, String timezone).
        CalendarDTO result = calendarService.createCalendar(user.getId(), input.getName(), input.getTimezone());

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("EST", result.getTimezone());
    }
}
