package com.calendar.config;

import com.calendar.middleware.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    @Autowired
    private com.calendar.middleware.RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    private com.calendar.middleware.LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**"); 
        
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**");
    }
}
