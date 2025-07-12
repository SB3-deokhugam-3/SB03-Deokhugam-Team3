package com.sprint.deokhugam.global.exception;

/**
 * 400 Bad Request 예외의 상위 클래스
 */
abstract class BadRequestException extends DeokhugamException {

    // 기본: INVALID_ 접두사
    protected BadRequestException(String domain, String message) {
        super("INVALID_" + domain, message);
    }

    // 커스텀 에러 코드도 허용
    protected BadRequestException(String errorCode, String action, String message) {
        super(errorCode, message);  // 예: "DUPLICATE_REVIEW"
    }
}
