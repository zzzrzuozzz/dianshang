package com.dianshang.admin.stats.support;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record StatsRange(LocalDate start, LocalDate end) {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public static StatsRange resolve(String range, String startDate, String endDate) {
        LocalDate end = LocalDate.now();
        if (startDate != null && !startDate.isBlank() && endDate != null && !endDate.isBlank()) {
            return new StatsRange(LocalDate.parse(startDate, ISO), LocalDate.parse(endDate, ISO));
        }
        int days = "month".equalsIgnoreCase(range) ? 30 : 7;
        return new StatsRange(end.minusDays(days - 1L), end);
    }

    public LocalDateTime startAt() {
        return start.atStartOfDay();
    }

    public LocalDateTime endExclusive() {
        return end.plusDays(1).atStartOfDay();
    }

    public boolean contains(LocalDateTime time) {
        if (time == null) {
            return false;
        }
        LocalDate d = time.toLocalDate();
        return !d.isBefore(start) && !d.isAfter(end);
    }
}
