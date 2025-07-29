package com.sprint.deokhugam.global.exception;

import java.time.Instant;
import java.util.Map;
import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    private final Instant timestamp;
    private final ErrorCode baseErrorCode;
    private final String errorCode;      // 도메인 + 에러 코드 (BOOK_NOT_FOUND)
    private final String errorMessage;   // 도메인 + 메시지 (BOOK 찾을 수 없습니다)
    private final Map<String, Object> details;

    // 1. 도메인 있을 때
    protected DomainException(String domain, ErrorCode baseErrorCode,
        Map<String, Object> details) {
        super(baseErrorCode.getMessageWithDomain(domain));
        this.timestamp = Instant.now();
        this.baseErrorCode = baseErrorCode;
        this.errorCode = baseErrorCode.getCodeWithDomain(domain);  // BOOK_NOT_FOUND
        this.errorMessage = baseErrorCode.getMessageWithDomain(domain);  // BOOK 찾을 수 없습니다
        this.details = details != null ? details : Map.of();
    }

    // 2. 없을 때
    protected DomainException(ErrorCode baseErrorCode, Map<String, Object> details) {
        super(baseErrorCode.getMessage());
        this.timestamp = Instant.now();
        this.baseErrorCode = baseErrorCode;
        this.errorCode = baseErrorCode.getCode();  // NOT_FOUND
        this.errorMessage = baseErrorCode.getMessage();  // 찾을 수 없습니다
        this.details = details != null ? details : Map.of();
    }

    public ErrorCode getErrorCode() {
        return baseErrorCode;
    }

    public String getErrorCodeString() {
        return errorCode;
    }
}
