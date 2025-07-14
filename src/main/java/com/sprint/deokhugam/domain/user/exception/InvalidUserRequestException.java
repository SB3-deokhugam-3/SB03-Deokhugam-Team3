package com.sprint.deokhugam.domain.user.exception;

import com.sprint.deokhugam.global.exception.BadRequestException;
import java.util.Map;

public class InvalidUserRequestException extends BadRequestException {

    public InvalidUserRequestException(String field, String message) {
        super("USER", Map.of(field, message));
    }
}