package com.sprint.deokhugam.domain.user.exception;

import com.sprint.deokhugam.global.exception.ConflictException;
import java.util.Map;

public class DuplicateEmailException extends ConflictException {

    private final String message;

    public DuplicateEmailException(String email, String message) {
        super("USER", Map.of("email", email));
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}