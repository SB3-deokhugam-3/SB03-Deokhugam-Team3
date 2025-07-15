package com.sprint.deokhugam.global.exception;

import java.util.Map;

/**
 * 401 Unauthorized 예외의 상위 클래스
 */
public class UnauthorizedException extends DeokhugamException {

    public UnauthorizedException(String domain, Map<String, Object> details) {
        super(domain, ErrorCode.UNAUTHORIZED, details);
    }

    public UnauthorizedException(Map<String, Object> details) {
        super(ErrorCode.UNAUTHORIZED, details);
    }
}