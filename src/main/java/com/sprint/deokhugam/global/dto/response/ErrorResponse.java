package com.sprint.deokhugam.global.dto.response;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    Instant timestamp,
    String code,                    // 추가: 에러 코드
    String message,
    Map<String, Object> details,    // 변경: String → Map
    String exceptionType,           // 추가: 예외 타입
    int status
) {

    // 기본 생성 메서드
    public static ErrorResponse of(String code, String message, int status) {
        return new ErrorResponse(
            Instant.now(),
            code,
            message,
            Map.of(),
            "Exception",
            status
        );
    }

    // details가 있는 생성 메서드
    public static ErrorResponse of(String code, String message, Map<String, Object> details,
        int status) {
        return new ErrorResponse(
            Instant.now(),
            code,
            message,
            details != null ? details : Map.of(),
            "Exception",
            status
        );
    }
}