package com.calendar.dto;

import java.time.LocalDate;
import java.util.List;

public class RecurrenceDTO {
    private String pattern; // DAILY, WEEKLY, MONTHLY
    private Integer interval; // every X days/weeks
    private List<String> daysOfWeek; // MONDAY, TUESDAY, etc.
    private String type; // COUNT or DATE
    private Integer occurrences;
    private LocalDate untilDate;

    public RecurrenceDTO() {
    }

    public RecurrenceDTO(String pattern, Integer interval, List<String> daysOfWeek, String type, Integer occurrences, LocalDate untilDate) {
        this.pattern = pattern;
        this.interval = interval;
        this.daysOfWeek = daysOfWeek;
        this.type = type;
        this.occurrences = occurrences;
        this.untilDate = untilDate;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public List<String> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<String> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(Integer occurrences) {
        this.occurrences = occurrences;
    }

    public LocalDate getUntilDate() {
        return untilDate;
    }

    public void setUntilDate(LocalDate untilDate) {
        this.untilDate = untilDate;
    }
}
