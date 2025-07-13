package com.sprint.deokhugam.global.dto.response;

import com.sprint.deokhugam.global.exception.DeokhugamException;
import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    Instant timestamp,
    String code,
    String message,
    Map<String, Object> details,
    String exceptionType,
    int status
) {

    public static ErrorResponse of(DeokhugamException ex) {
        return new ErrorResponse(
            ex.getTimestamp(),
            ex.getErrorCodeString(),
            ex.getErrorMessage(),
            ex.getDetails(),
            ex.getClass().getSimpleName(),
            ex.getErrorCode().getStatus()
        );
    }
}