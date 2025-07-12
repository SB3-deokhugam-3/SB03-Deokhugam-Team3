package com.sprint.deokhugam.global.exception;

/**
 * 409 Conflict 예외의 상위 클래스
 */
abstract class ConflictException extends DeokhugamException {

    // 기본: _ALREADY_EXISTS
    protected ConflictException(String domain, String message) {
        super(domain + "_ALREADY_EXISTS", message);
    }

    // 커스텀 에러 코드도 허용
    protected ConflictException(String domain, String action, String message) {
        super(action + "_" + domain, message);
    }
}
