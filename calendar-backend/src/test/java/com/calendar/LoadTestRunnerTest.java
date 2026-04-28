package com.calendar;

import com.calendar.model.Calendar;
import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.repository.CalendarRepository;
import com.calendar.repository.EventRepository;
import com.calendar.repository.UserRepository;
import com.calendar.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoadTestRunnerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void loadTestData_RunsSuccessfully() throws Exception {
        LoadTestRunner runner = new LoadTestRunner();

        // Stubbing
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        Calendar mockCalendar = new Calendar();
        mockCalendar.setUser(mockUser);
        when(calendarRepository.save(any(Calendar.class))).thenReturn(mockCalendar);

        // Capture events to avoid verifying 10000 individual saves
        // Actually the code calls saveAll multiple times
        when(eventRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        CommandLineRunner commandLineRunner = runner.loadTestData(
                userRepository, calendarRepository, eventRepository, analyticsService, passwordEncoder);

        // Run logic
        commandLineRunner.run();

        // Verifications
        verify(userRepository).deleteAll();
        verify(eventRepository).deleteAll();
        verify(userRepository, atLeastOnce()).save(any(User.class));
        verify(calendarRepository, atLeastOnce()).save(any(Calendar.class));
        verify(eventRepository, atLeastOnce()).saveAll(anyList());
        
        // Verify analytics test
        verify(analyticsService, atLeastOnce()).getUserAnalytics(anyLong());
    }
}
