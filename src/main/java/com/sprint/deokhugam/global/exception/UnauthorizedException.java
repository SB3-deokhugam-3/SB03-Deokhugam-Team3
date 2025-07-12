package com.sprint.deokhugam.global.exception;

/**
 * 401 Unauthorized 예외의 상위 클래스
 */
abstract class UnauthorizedException extends DeokhugamException {

    protected UnauthorizedException(String errorCode, String message) {
        super(errorCode, message);
    }

}