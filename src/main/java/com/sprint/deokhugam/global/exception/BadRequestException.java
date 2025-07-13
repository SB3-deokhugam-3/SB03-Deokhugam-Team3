package com.sprint.deokhugam.global.exception;

import java.util.Map;

/**
 * 400 Bad Request 예외의 상위 클래스
 */
public class BadRequestException extends DeokhugamException {
    public BadRequestException(String domain, Map<String, Object> details) {
        super(domain, ErrorCode.INVALID_INPUT_VALUE, details);
    }

    public BadRequestException(Map<String, Object> details) {
        super(ErrorCode.INVALID_INPUT_VALUE, details);
    }
}
