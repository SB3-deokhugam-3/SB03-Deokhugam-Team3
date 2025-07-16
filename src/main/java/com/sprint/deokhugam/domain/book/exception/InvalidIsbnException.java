package com.sprint.deokhugam.domain.book.exception;

import com.sprint.deokhugam.global.exception.BadRequestException;
import java.util.Map;

public class InvalidIsbnException extends BadRequestException {

    public InvalidIsbnException(String isbn) {
        super(Map.of("잘못된 ISBN 형식입니다.", isbn));
    }
}
