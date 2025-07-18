package com.sprint.deokhugam.domain.user.exception;

import com.sprint.deokhugam.global.exception.MissingHeaderException;
import java.util.Map;

public class MissingUserIdHeaderException extends MissingHeaderException {

    public MissingUserIdHeaderException(String message) {
        super(Map.of("message", message));
    }
}
