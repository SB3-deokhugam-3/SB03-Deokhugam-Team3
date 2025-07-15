package com.sprint.deokhugam.domain.book.exception;

import com.sprint.deokhugam.global.exception.NotFoundException;
import java.util.Map;
import java.util.UUID;

public class BookNotFoundException extends NotFoundException {

    public BookNotFoundException(UUID bookId) {
        super("BOOK", Map.of("bookId", bookId));
    }
}
