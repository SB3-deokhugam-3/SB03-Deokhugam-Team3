package com.sprint.deokhugam.global.period;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public enum PeriodType {
    DAILY {
        @Override
        public Instant getStartInstant(Instant today, ZoneId zone) {
            LocalDate date = LocalDate.ofInstant(today, zone);
            return date.atStartOfDay(zone).toInstant();
        }

        @Override
        public Instant getEndInstant(Instant today, ZoneId zone) {
            LocalDate date = LocalDate.ofInstant(today, zone).plusDays(1);
            return date.atStartOfDay(zone).toInstant();
        }
    },
    WEEKLY {
        @Override
        public Instant getStartInstant(Instant today, ZoneId zone) {
            LocalDate date = LocalDate.ofInstant(today, zone)
                .with(DayOfWeek.MONDAY);
            return date.atStartOfDay(zone).toInstant();
        }

        @Override
        public Instant getEndInstant(Instant today, ZoneId zone) {
            LocalDate date = LocalDate.ofInstant(today, zone)
                .with(DayOfWeek.MONDAY).plusWeeks(1);
            return date.atStartOfDay(zone).toInstant();
        }
    },
    MONTHLY {
        @Override
        public Instant getStartInstant(Instant today, ZoneId zone) {
            LocalDate date = LocalDate.ofInstant(today, zone)
                .withDayOfMonth(1);
            return date.atStartOfDay(zone).toInstant();
        }

        @Override
        public Instant getEndInstant(Instant today, ZoneId zone) {
            LocalDate date = LocalDate.ofInstant(today, zone)
                .withDayOfMonth(1).plusMonths(1);
            return date.atStartOfDay(zone).toInstant();
        }
    },
    ALL_TIME {
        @Override
        public Instant getStartInstant(Instant today, ZoneId zone) {
            return Instant.EPOCH; // 1970-01-01T00:00:00Z
        }

        @Override
        public Instant getEndInstant(Instant today, ZoneId zone) {
            return Instant.MAX;
        }
    };

    public abstract Instant getStartInstant(Instant today, ZoneId zone);
    public abstract Instant getEndInstant(Instant today, ZoneId zone);
}

