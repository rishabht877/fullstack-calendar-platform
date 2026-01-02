package com.calendar.dto;

import java.time.LocalDateTime;

public class EventDTO {
    private Long id;
    private String subject;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private String location;
    private String status;
    private String seriesId;
    private Long calendarId;
    private RecurrenceDTO recurrence;

    public EventDTO() {
    }

    public EventDTO(Long id, String subject, LocalDateTime startTime, LocalDateTime endTime, String description, String location, String status, String seriesId, Long calendarId, RecurrenceDTO recurrence) {
        this.id = id;
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.location = location;
        this.status = status;
        this.seriesId = seriesId;
        this.calendarId = calendarId;
        this.recurrence = recurrence;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    public RecurrenceDTO getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(RecurrenceDTO recurrence) {
        this.recurrence = recurrence;
    }

    public static class Builder {
        private Long id;
        private String subject;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String description;
        private String location;
        private String status;
        private String seriesId;
        private Long calendarId;
        private RecurrenceDTO recurrence;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder seriesId(String seriesId) {
            this.seriesId = seriesId;
            return this;
        }

        public Builder calendarId(Long calendarId) {
            this.calendarId = calendarId;
            return this;
        }

        public Builder recurrence(RecurrenceDTO recurrence) {
            this.recurrence = recurrence;
            return this;
        }

        public EventDTO build() {
            return new EventDTO(id, subject, startTime, endTime, description, location, status, seriesId, calendarId, recurrence);
        }
    }
}
