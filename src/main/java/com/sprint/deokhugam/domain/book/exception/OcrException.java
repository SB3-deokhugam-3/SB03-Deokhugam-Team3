package com.sprint.deokhugam.domain.book.exception;

import com.sprint.deokhugam.global.exception.DeokhugamException;
import com.sprint.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class OcrException extends DeokhugamException {
    public OcrException(String message) {
        super("OCR", ErrorCode.INTERNAL_SERVER_ERROR, Map.of("message", message));
    }

    public OcrException(String message, Throwable cause) {
        super("OCR", ErrorCode.INTERNAL_SERVER_ERROR, Map.of("message", message, "cause", cause.getMessage()));
    }
}
