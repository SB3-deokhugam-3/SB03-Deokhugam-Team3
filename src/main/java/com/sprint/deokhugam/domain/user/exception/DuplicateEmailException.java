package com.sprint.deokhugam.domain.user.exception;

import com.sprint.deokhugam.global.exception.ConflictException;
import java.util.Map;

public class DuplicateEmailException extends ConflictException {

    public DuplicateEmailException(String email) {
        super("USER", Map.of("email", email));
    }
}