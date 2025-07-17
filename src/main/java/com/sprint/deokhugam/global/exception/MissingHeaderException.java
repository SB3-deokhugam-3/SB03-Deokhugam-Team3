package com.sprint.deokhugam.global.exception;

import java.util.Map;

/**
 * Header 값 누락 예외의 상위 클래스
 */
public class MissingHeaderException extends DomainException {

    public MissingHeaderException(String domain, Map<String, Object> details) {
        super(domain, ErrorCode.MISSING_REQUEST_HEADER, details);
    }

    public MissingHeaderException(Map<String, Object> details) {
        super(ErrorCode.MISSING_REQUEST_HEADER, details);
    }
}
