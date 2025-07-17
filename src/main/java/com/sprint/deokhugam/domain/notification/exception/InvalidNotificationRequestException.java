package com.sprint.deokhugam.domain.notification.exception;

import com.sprint.deokhugam.global.exception.BadRequestException;
import java.util.Map;

public class InvalidNotificationRequestException extends BadRequestException {

    private final String message;

    public InvalidNotificationRequestException(String field, String message) {
        super("NOTIFICATION", Map.of(field, message));
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}