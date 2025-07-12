package com.sprint.deokhugam.global.exception;

import java.util.Map;

/**
 * 409 Conflict 예외의 상위 클래스
 */
public class ConflictException extends DeokhugamException {
    public ConflictException(String domain, Map<String, Object> details) {
        super(domain, ErrorCode.DUPLICATE_RESOURCE, details);
    }

    public ConflictException(Map<String, Object> details) {
        super(ErrorCode.DUPLICATE_RESOURCE, details);
    }
}
