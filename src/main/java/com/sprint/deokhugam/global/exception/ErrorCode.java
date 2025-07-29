package com.sprint.deokhugam.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    NOT_FOUND("찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_INPUT_VALUE("잘못된 입력 값입니다.", HttpStatus.BAD_REQUEST),
    INVALID_TYPE_VALUE("지원하지 않는 타입입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_RESOURCE("이미 존재하는 자원입니다.", HttpStatus.CONFLICT),
    FORBIDDEN_ACTION("이 작업을 수행할 권한이 없습니다", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    MISSING_REQUEST_HEADER("필수 헤더값을 누락했습니다.", HttpStatus.BAD_REQUEST),
    NO_RESOURCE_FOUND("요청하신 경로를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MISSING_REQUEST_PARAMETER("필수 요청 파라미터가 없습니다.", HttpStatus.BAD_REQUEST),
    BATCH_ALREADY_RUN("이미 작업이 완료된 배치입니다.", null); // 배치는 http request랑 관련없음

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getStatus() {
        return httpStatus.value();
    }

    // 도메인과 함께 메시지 생성
    public String getMessageWithDomain(String domain) {
        return domain + " " + message;
    }

    // 도메인과 함께 에러 코드 생성
    public String getCodeWithDomain(String domain) {
        return domain + "_" + this.name();
    }

    // 도메인 없을 때: "NOT_FOUND"
    public String getCode() {
        return this.name();
    }
}
