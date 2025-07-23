package com.sprint.deokhugam.global.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class TimeUtils {

    private TimeUtils() {
    } // 새로운 인스턴스 생성 제한

    public static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    // Instant -> ZonedDateTime 변환
    public static ZonedDateTime toZonedDateTime(Instant instant) {
        return instant.atZone(SEOUL_ZONE_ID);
    }

    // ZonedDateTime -> Instant 변환
    public static Instant toInstant(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant();
    }

    // Instant -> LocalDate(년/월/일까지만) 변환
    public static LocalDate toLocalDate(Instant instant) {
        return instant.atZone(SEOUL_ZONE_ID).toLocalDate();
    }


    // 오늘의 시작 시간 (00:00)
    public static ZonedDateTime startOfDayWithZone(Instant referenceTime) {
        ZonedDateTime zoned = referenceTime.atZone(SEOUL_ZONE_ID);
        return zoned.toLocalDate().atStartOfDay(SEOUL_ZONE_ID);
    }

    // 오늘의 끝 (23:59:59.999999999)
    public static ZonedDateTime endOfDayWithZone(Instant referenceTime) {
        return startOfDayWithZone(referenceTime).plusDays(1).minusNanos(1);
    }
}
