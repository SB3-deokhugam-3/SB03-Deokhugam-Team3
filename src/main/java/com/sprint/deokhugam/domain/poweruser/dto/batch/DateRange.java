package com.sprint.deokhugam.domain.poweruser.dto.batch;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Spring Batch용 날짜 범위 객체
 * 각 기간별 데이터 조회 범위를 정의하는 불변 객체
 */
public record DateRange(
    Instant startDate,
    Instant endDate
) {
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 어제 범위 (00:00:00 ~ 23:59:59)
     */
    public static DateRange yesterday() {
        LocalDateTime endTime = LocalDateTime.now(KOREA_ZONE)
            .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startTime = endTime.minusDays(1);

        return new DateRange(
            startTime.atZone(KOREA_ZONE).toInstant(),
            endTime.atZone(KOREA_ZONE).toInstant()
        );
    }

    /**
     * 지난 주 범위 (7일 전 ~ 현재)
     */
    public static DateRange lastWeek() {
        Instant endTime = Instant.now();
        Instant startTime = endTime.minusSeconds(7 * 24 * 60 * 60); // 7일 전

        return new DateRange(startTime, endTime);
    }

    /**
     * 지난 달 범위 (30일 전 ~ 현재)
     */
    public static DateRange lastMonth() {
        Instant endTime = Instant.now();
        Instant startTime = endTime.minusSeconds(30L * 24 * 60 * 60); // 30일 전

        return new DateRange(startTime, endTime);
    }

    /**
     * 전체 기간 (제한 없음)
     */
    public static DateRange allTime() {
        return new DateRange(null, null);
    }

    /**
     * 기간 범위가 유효한지 확인
     */
    public boolean isValid() {
        if (startDate == null || endDate == null) {
            return true; // 전체 범위는 유효
        }
        return startDate.isBefore(endDate);
    }

    /**
     * 전체 기간인지 확인
     */
    public boolean isAllTime() {
        return startDate == null && endDate == null;
    }

    /**
     * 읽기 쉬운 문자열 형태로 변환 (로깅용)
     */
    public String toReadableString() {
        if (isAllTime()) {
            return "전체 기간";
        }

        String start = startDate != null ?
            LocalDateTime.ofInstant(startDate, KOREA_ZONE).format(FORMATTER) : "시작일 없음";
        String end = endDate != null ?
            LocalDateTime.ofInstant(endDate, KOREA_ZONE).format(FORMATTER) : "종료일 없음";

        return String.format("%s ~ %s", start, end);
    }
}