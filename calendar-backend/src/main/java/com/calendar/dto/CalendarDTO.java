package com.calendar.dto;

import java.time.LocalDateTime;

public class CalendarDTO {
    private Long id;
    private String name;
    private String timezone;
    private String color;
    private Long userId;

    public CalendarDTO() {
    }

    public CalendarDTO(Long id, String name, String timezone, String color, Long userId) {
        this.id = id;
        this.name = name;
        this.timezone = timezone;
        this.color = color;
        this.userId = userId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public static class Builder {
        private Long id;
        private String name;
        private String timezone;
        private String color;
        private Long userId;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public CalendarDTO build() {
            return new CalendarDTO(id, name, timezone, color, userId);
        }
    }
}
