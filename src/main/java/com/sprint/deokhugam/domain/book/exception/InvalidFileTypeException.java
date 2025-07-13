package com.sprint.deokhugam.domain.book.exception;

import com.sprint.deokhugam.global.exception.BadRequestException;
import java.util.Map;

public class InvalidFileTypeException extends BadRequestException {

    public InvalidFileTypeException(String contentType) {
        super("FILE", Map.of("contentType", contentType));
    }
}
