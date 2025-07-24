package com.sprint.deokhugam.global.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public final class TimeUtils {

    private TimeUtils() {
    } // 새로운 인스턴스 생성 제한

    public static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    // Instant -> LocalDate(년/월/일까지만) 변환
    public static LocalDate toLocalDate(Instant instant) {
        return instant.atZone(SEOUL_ZONE_ID).toLocalDate();
    }

}
