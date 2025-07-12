package com.sprint.deokhugam.global.exception;

import java.util.Map;

/**
 * 403 Forbidden 예외의 상위 클래스
 */
public class ForbiddenException extends DeokhugamException {
    public ForbiddenException(String domain, Map<String, Object> details) {
        super(domain, ErrorCode.FORBIDDEN_ACTION, details);
    }

    public ForbiddenException(Map<String, Object> details) {
        super(ErrorCode.FORBIDDEN_ACTION, details);
    }
}
