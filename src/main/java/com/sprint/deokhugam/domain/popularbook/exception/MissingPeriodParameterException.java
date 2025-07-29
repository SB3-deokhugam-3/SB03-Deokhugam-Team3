package com.sprint.deokhugam.domain.popularbook.exception;

import com.sprint.deokhugam.global.exception.BadRequestException;
import java.util.Map;

public class MissingPeriodParameterException extends BadRequestException {

    public MissingPeriodParameterException(String message) {
        super(Map.of("message", message));
    }
}
