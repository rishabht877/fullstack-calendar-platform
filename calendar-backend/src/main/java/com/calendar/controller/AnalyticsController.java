package com.calendar.controller;

import com.calendar.dto.AnalyticsDTO;
import com.calendar.repository.UserRepository;
import com.calendar.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public AnalyticsDTO getAnalytics() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userRepository.findByUsername(userDetails.getUsername()).get().getId();
        return analyticsService.getUserAnalytics(userId);
    }
}
