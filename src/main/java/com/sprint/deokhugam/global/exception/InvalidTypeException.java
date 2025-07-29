package com.sprint.deokhugam.global.exception;

import java.util.Map;

/**
 * 400 Bad request 예외의 상위 클래스
 */
public class InvalidTypeException extends DomainException {

    public InvalidTypeException(String domain, Map<String, Object> details) {
        super(domain, ErrorCode.INVALID_TYPE_VALUE, details);
    }

    public InvalidTypeException(Map<String, Object> details) {
        super(ErrorCode.INVALID_TYPE_VALUE, details);
    }
}
