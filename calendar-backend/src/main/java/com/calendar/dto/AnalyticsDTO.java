package com.calendar.dto;

import java.util.Map;

public class AnalyticsDTO {
    private long totalEvents;
    private long weekEvents;
    private long monthEvents;
    private long upcomingEvents;
    private long totalCalendars;
    private Map<String, Long> eventsByStatus;
    private double averageEventsPerDay;
    private String busiestDayOfWeek;
    private Map<String, Long> eventsBySubject;
    private Map<String, Long> eventsByWeekday;
    private String leastBusyDayOfWeek;
    private double onlinePercentage;

    public AnalyticsDTO() {
    }

    public AnalyticsDTO(long totalEvents, long weekEvents, long monthEvents, long upcomingEvents, 
                        long totalCalendars, Map<String, Long> eventsByStatus, double averageEventsPerDay, 
                        String busiestDayOfWeek, Map<String, Long> eventsBySubject, 
                        Map<String, Long> eventsByWeekday, String leastBusyDayOfWeek, double onlinePercentage) {
        this.totalEvents = totalEvents;
        this.weekEvents = weekEvents;
        this.monthEvents = monthEvents;
        this.upcomingEvents = upcomingEvents;
        this.totalCalendars = totalCalendars;
        this.eventsByStatus = eventsByStatus;
        this.averageEventsPerDay = averageEventsPerDay;
        this.busiestDayOfWeek = busiestDayOfWeek;
        this.eventsBySubject = eventsBySubject;
        this.eventsByWeekday = eventsByWeekday;
        this.leastBusyDayOfWeek = leastBusyDayOfWeek;
        this.onlinePercentage = onlinePercentage;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters for existing fields...
    public long getTotalEvents() { return totalEvents; }
    public void setTotalEvents(long totalEvents) { this.totalEvents = totalEvents; }
    
    public long getWeekEvents() { return weekEvents; }
    public void setWeekEvents(long weekEvents) { this.weekEvents = weekEvents; }
    
    public long getMonthEvents() { return monthEvents; }
    public void setMonthEvents(long monthEvents) { this.monthEvents = monthEvents; }
    
    public long getUpcomingEvents() { return upcomingEvents; }
    public void setUpcomingEvents(long upcomingEvents) { this.upcomingEvents = upcomingEvents; }
    
    public long getTotalCalendars() { return totalCalendars; }
    public void setTotalCalendars(long totalCalendars) { this.totalCalendars = totalCalendars; }
    
    public Map<String, Long> getEventsByStatus() { return eventsByStatus; }
    public void setEventsByStatus(Map<String, Long> eventsByStatus) { this.eventsByStatus = eventsByStatus; }
    
    public double getAverageEventsPerDay() { return averageEventsPerDay; }
    public void setAverageEventsPerDay(double averageEventsPerDay) { this.averageEventsPerDay = averageEventsPerDay; }
    
    public String getBusiestDayOfWeek() { return busiestDayOfWeek; }
    public void setBusiestDayOfWeek(String busiestDayOfWeek) { this.busiestDayOfWeek = busiestDayOfWeek; }

    // New Getters and Setters
    public Map<String, Long> getEventsBySubject() { return eventsBySubject; }
    public void setEventsBySubject(Map<String, Long> eventsBySubject) { this.eventsBySubject = eventsBySubject; }

    public Map<String, Long> getEventsByWeekday() { return eventsByWeekday; }
    public void setEventsByWeekday(Map<String, Long> eventsByWeekday) { this.eventsByWeekday = eventsByWeekday; }

    public String getLeastBusyDayOfWeek() { return leastBusyDayOfWeek; }
    public void setLeastBusyDayOfWeek(String leastBusyDayOfWeek) { this.leastBusyDayOfWeek = leastBusyDayOfWeek; }

    public double getOnlinePercentage() { return onlinePercentage; }
    public void setOnlinePercentage(double onlinePercentage) { this.onlinePercentage = onlinePercentage; }


    public static class Builder {
        private long totalEvents;
        private long weekEvents;
        private long monthEvents;
        private long upcomingEvents;
        private long totalCalendars;
        private Map<String, Long> eventsByStatus;
        private double averageEventsPerDay;
        private String busiestDayOfWeek;
        private Map<String, Long> eventsBySubject;
        private Map<String, Long> eventsByWeekday;
        private String leastBusyDayOfWeek;
        private double onlinePercentage;

        public Builder totalEvents(long totalEvents) {
            this.totalEvents = totalEvents;
            return this;
        }

        public Builder weekEvents(long weekEvents) {
            this.weekEvents = weekEvents;
            return this;
        }

        public Builder monthEvents(long monthEvents) {
            this.monthEvents = monthEvents;
            return this;
        }

        public Builder upcomingEvents(long upcomingEvents) {
            this.upcomingEvents = upcomingEvents;
            return this;
        }

        public Builder totalCalendars(long totalCalendars) {
            this.totalCalendars = totalCalendars;
            return this;
        }

        public Builder eventsByStatus(Map<String, Long> eventsByStatus) {
            this.eventsByStatus = eventsByStatus;
            return this;
        }

        public Builder averageEventsPerDay(double averageEventsPerDay) {
            this.averageEventsPerDay = averageEventsPerDay;
            return this;
        }

        public Builder busiestDayOfWeek(String busiestDayOfWeek) {
            this.busiestDayOfWeek = busiestDayOfWeek;
            return this;
        }

        public Builder eventsBySubject(Map<String, Long> eventsBySubject) {
            this.eventsBySubject = eventsBySubject;
            return this;
        }

        public Builder eventsByWeekday(Map<String, Long> eventsByWeekday) {
            this.eventsByWeekday = eventsByWeekday;
            return this;
        }

        public Builder leastBusyDayOfWeek(String leastBusyDayOfWeek) {
            this.leastBusyDayOfWeek = leastBusyDayOfWeek;
            return this;
        }

        public Builder onlinePercentage(double onlinePercentage) {
            this.onlinePercentage = onlinePercentage;
            return this;
        }

        public AnalyticsDTO build() {
            return new AnalyticsDTO(totalEvents, weekEvents, monthEvents, upcomingEvents, 
                totalCalendars, eventsByStatus, averageEventsPerDay, busiestDayOfWeek,
                eventsBySubject, eventsByWeekday, leastBusyDayOfWeek, onlinePercentage);
        }
    }
}
