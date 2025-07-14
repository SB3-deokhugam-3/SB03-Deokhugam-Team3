package com.sprint.deokhugam.domain.user.exception;

import com.sprint.deokhugam.global.exception.NotFoundException;
import java.util.Map;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String userId) {
        super("USER", Map.of("userId", userId));
    }
}