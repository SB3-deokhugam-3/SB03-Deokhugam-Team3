package com.sprint.deokhugam.domain.book.exception;

import com.sprint.deokhugam.global.exception.BadRequestException;
import java.util.Map;
import java.util.UUID;

public class BookNotSoftDeletedException extends BadRequestException {

    public BookNotSoftDeletedException(UUID bookId) {
        super("BOOK_NOT_SOFT_DELETED", Map.of("bookId", bookId));
    }
}


