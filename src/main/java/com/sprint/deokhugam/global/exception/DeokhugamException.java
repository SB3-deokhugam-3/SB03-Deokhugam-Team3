package com.sprint.deokhugam.global.exception;

import java.util.Map;

abstract class DeokhugamException extends RuntimeException {

    private final String errorCode;
    private final Map<String, Object> details;

    protected DeokhugamException(String errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    protected DeokhugamException(String errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details != null ? details : Map.of();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
