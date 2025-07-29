package com.sprint.deokhugam.domain.book.exception;

import com.sprint.deokhugam.global.exception.ConflictException;
import java.util.Map;

public class DuplicateIsbnException extends ConflictException {

    public DuplicateIsbnException(String isbn) {
        super("BOOK", Map.of("isbn", isbn));
    }
}
