package com.calendar;

import com.calendar.model.Calendar;
import com.calendar.model.Event;
import com.calendar.model.User;
import com.calendar.repository.CalendarRepository;
import com.calendar.repository.EventRepository;
import com.calendar.repository.UserRepository;
import com.calendar.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Load testing component to populate database with 10,000+ events
 * and measure actual performance metrics.
 * 
 * Run with: mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=loadtest
 */
@Component
@Profile("loadtest")
public class LoadTestRunner {

    private static final Logger logger = LoggerFactory.getLogger(LoadTestRunner.class);
    private static final int TARGET_EVENTS = 10000;
    private static final int NUM_USERS = 10;
    private static final int NUM_CALENDARS_PER_USER = 3;

    @Bean
    public CommandLineRunner loadTestData(
            UserRepository userRepository,
            CalendarRepository calendarRepository,
            EventRepository eventRepository,
            AnalyticsService analyticsService,
            PasswordEncoder passwordEncoder) {

        return args -> {
            logger.info("=== Starting Load Test Data Generation ===");
            long startTime = System.currentTimeMillis();

            // Clear existing data
            logger.info("Clearing existing data...");
            eventRepository.deleteAll();
            calendarRepository.deleteAll();
            userRepository.deleteAll();

            // Create users
            logger.info("Creating {} users...", NUM_USERS);
            List<User> users = new ArrayList<>();
            for (int i = 0; i < NUM_USERS; i++) {
                User user = new User(
                        "loadtest_user_" + i,
                        "loadtest" + i + "@example.com",
                        passwordEncoder.encode("password123")
                );
                users.add(userRepository.save(user));
            }

            // Create calendars
            logger.info("Creating {} calendars per user...", NUM_CALENDARS_PER_USER);
            List<Calendar> calendars = new ArrayList<>();
            String[] timezones = {"UTC", "America/New_York", "America/Los_Angeles", "Europe/London"};
            for (User user : users) {
                for (int i = 0; i < NUM_CALENDARS_PER_USER; i++) {
                    Calendar calendar = new Calendar(
                            "Calendar " + i + " - " + user.getUsername(),
                            timezones[i % timezones.length],
                            user
                    );
                    calendars.add(calendarRepository.save(calendar));
                }
            }

            // Create events
            logger.info("Creating {} events...", TARGET_EVENTS);
            Random random = new Random();
            String[] subjects = {
                    "Team Meeting", "Client Call", "Code Review", "Sprint Planning",
                    "1-on-1", "All Hands", "Training Session", "Workshop",
                    "Project Sync", "Design Review", "Standup", "Retrospective"
            };
            String[] locations = {
                    "Conference Room A", "Zoom", "Office", "https://meet.google.com/abc",
                    "Building 2", "https://zoom.us/j/123456", "Remote", "Cafeteria"
            };
            String[] statuses = {"CONFIRMED", "TENTATIVE", "CANCELLED"};

            List<Event> events = new ArrayList<>();
            LocalDateTime baseDate = LocalDateTime.now().minusMonths(6);

            for (int i = 0; i < TARGET_EVENTS; i++) {
                Calendar calendar = calendars.get(random.nextInt(calendars.size()));
                
                // Random date within 1 year range
                LocalDateTime eventStartTime = baseDate.plusDays(random.nextInt(365))
                        .withHour(random.nextInt(16) + 8) // 8 AM to 11 PM
                        .withMinute(random.nextInt(4) * 15); // 0, 15, 30, 45
                
                LocalDateTime endTime = eventStartTime.plusHours(1);

                Event event = new Event(
                        subjects[random.nextInt(subjects.length)],
                        eventStartTime,
                        endTime,
                        calendar
                );
                event.setDescription("Load test event #" + i);
                event.setLocation(locations[random.nextInt(locations.length)]);
                event.setStatus(statuses[random.nextInt(statuses.length)]);

                events.add(event);

                // Batch save every 1000 events
                if (events.size() >= 1000) {
                    eventRepository.saveAll(events);
                    logger.info("Saved {} events...", i + 1);
                    events.clear();
                }
            }

            // Save remaining events
            if (!events.isEmpty()) {
                eventRepository.saveAll(events);
            }

            long loadTime = System.currentTimeMillis() - startTime;
            logger.info("=== Load Test Data Generation Complete ===");
            logger.info("Total time: {}ms", loadTime);
            logger.info("Users created: {}", NUM_USERS);
            logger.info("Calendars created: {}", NUM_USERS * NUM_CALENDARS_PER_USER);
            logger.info("Events created: {}", TARGET_EVENTS);

            // Test analytics performance
            logger.info("\n=== Testing Analytics Performance ===");
            testAnalyticsPerformance(users, analyticsService);

            logger.info("\n=== Load Test Complete ===");
            logger.info("View metrics at: http://localhost:8080/actuator/metrics");
            logger.info("View cache stats at: http://localhost:8080/actuator/caches");
        };
    }

    private void testAnalyticsPerformance(List<User> users, AnalyticsService analyticsService) {
        int iterations = 20;
        List<Long> executionTimes = new ArrayList<>();

        for (User user : users) {
            for (int i = 0; i < iterations; i++) {
                long start = System.currentTimeMillis();
                analyticsService.getUserAnalytics(user.getId());
                long executionTime = System.currentTimeMillis() - start;
                executionTimes.add(executionTime);

                if (i == 0) {
                    logger.info("User {} - First call (cache miss): {}ms", user.getId(), executionTime);
                } else if (i == 1) {
                    logger.info("User {} - Second call (cache hit): {}ms", user.getId(), executionTime);
                }
            }
        }

        // Calculate statistics
        long sum = executionTimes.stream().mapToLong(Long::longValue).sum();
        double average = sum / (double) executionTimes.size();
        long min = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        long max = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        logger.info("\n=== Analytics Performance Results ===");
        logger.info("Total queries: {}", executionTimes.size());
        logger.info("Average execution time: {:.2f}ms", String.format("%.2f", average));
        logger.info("Min execution time: {}ms", min);
        logger.info("Max execution time: {}ms", max);
        logger.info("Estimated cache hit ratio: ~{}%", 
                String.format("%.1f", ((iterations - 1.0) / iterations) * 100));
    }
}
