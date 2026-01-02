package com.calendar.util;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Set;
import java.util.TreeSet;

public class TimezoneUtil {

    public static boolean isValidTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }

    public static Set<String> getAvailableTimezones() {
        return new TreeSet<>(ZoneId.getAvailableZoneIds());
    }
}
