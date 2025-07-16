package com.sprint.deokhugam.domain.book.exception;

import com.sprint.deokhugam.global.exception.BadRequestException;
import com.sprint.deokhugam.global.exception.DomainException;
import com.sprint.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class OcrException extends DomainException {

    private OcrException(String domain, ErrorCode errorCode, Map<String, Object> details) {
        super(domain, errorCode, details);
    }

    // 400번대 에러 - 클라이언트 에러 ( 잘못된 이미지 형식, 파일 문제 등 )
    public static OcrException clientError(String message) {
        return new OcrException("OCR", ErrorCode.INVALID_INPUT_VALUE, Map.of("message", message));
    }

    public static OcrException clientError(String message, Throwable cause) {
        return new OcrException("OCR", ErrorCode.INVALID_INPUT_VALUE,
            Map.of("message", message, "cause", cause.getMessage()));
    }

    // 500번대 에러 - 서버 에러 ( OCR 서비스 장애, 내부 처리 오류 등 )
    public static OcrException serverError(String message) {
        return new OcrException("OCR", ErrorCode.INTERNAL_SERVER_ERROR, Map.of("message", message));
    }

    public static OcrException serverError(String message, Throwable cause) {
        return new OcrException("OCR", ErrorCode.INTERNAL_SERVER_ERROR,
            Map.of("message", message, "cause", cause.getMessage()));
    }
}