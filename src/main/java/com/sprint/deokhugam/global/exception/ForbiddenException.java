package com.sprint.deokhugam.global.exception;

/**
 * 403 Forbidden 예외의 상위 클래스
 */
abstract class ForbiddenException extends DeokhugamException {

    protected ForbiddenException(String errorCode, String message) {
        super(errorCode, message);
    }

}
