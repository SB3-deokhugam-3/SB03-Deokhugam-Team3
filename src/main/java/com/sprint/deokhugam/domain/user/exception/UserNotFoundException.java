package com.sprint.deokhugam.domain.user.exception;

import com.sprint.deokhugam.global.exception.NotFoundException;
import java.util.Map;
import java.util.UUID;

public class UserNotFoundException extends NotFoundException {
    private final String message;

    public UserNotFoundException(UUID userId, String message) {
        super("USER", Map.of("userId", userId));
        this.message = message;
    }

    public UserNotFoundException(String email, String message) {
        super("USER", Map.of("email", email));
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}