package com.sprint.deokhugam.global.exception;

/**
 * 404 Not Found 예외의 상위 클래스
 */
abstract class NotFoundException extends DeokhugamException {

    // 기본: _NOT_FOUND
    protected NotFoundException(String domain, String message) {
        super(domain + "_NOT_FOUND", message);
    }

    // 커스텀 에러 코드도 허용
    protected NotFoundException(String domain, String action, String message) {
        super(action + "_" + domain, message);
    }
}
