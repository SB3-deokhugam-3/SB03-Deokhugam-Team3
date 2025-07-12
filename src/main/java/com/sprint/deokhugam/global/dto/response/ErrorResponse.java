package com.sprint.deokhugam.global.dto.response;

import com.sprint.deokhugam.global.exception.DeokhugamException;
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

    public static ErrorResponse of(DeokhugamException ex) {
        return new ErrorResponse(
            ex.getTimestamp(),
            ex.getErrorCodeString(),  // 문자열 에러 코드
            ex.getErrorMessage(),
            ex.getDetails(),
            ex.getClass().getSimpleName(),
            ex.getErrorCode().getStatus()
        );
    }
}