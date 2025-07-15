package com.sprint.deokhugam.domain.user.exception;

import com.sprint.deokhugam.global.exception.NotFoundException;
import java.util.Map;

public class UserNotFoundException extends NotFoundException {
    private final String message;

    public UserNotFoundException(String userId, String message) {
        super("USER", Map.of("userId", userId));
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}