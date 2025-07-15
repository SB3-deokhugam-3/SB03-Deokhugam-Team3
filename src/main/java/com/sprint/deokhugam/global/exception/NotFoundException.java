package com.sprint.deokhugam.global.exception;

import java.util.Map;

/**
 * 404 Not Found 예외의 상위 클래스
 */
public class NotFoundException extends DomainException {

    public NotFoundException(String domain, Map<String, Object> details) {
        super(domain, ErrorCode.NOT_FOUND, details);
    }

    public NotFoundException(Map<String, Object> details) {
        super(ErrorCode.NOT_FOUND, details);
    }
}
