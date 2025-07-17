package com.sprint.deokhugam.domain.book.exception;

import java.util.UUID;
import lombok.Getter;

@Getter
public class BookNotSoftDeletedException extends RuntimeException {
    private final UUID bookId;

    public BookNotSoftDeletedException(UUID bookId) {
        super("논리 삭제된 도서만 물리 삭제할 수 있습니다. bookId: " + bookId);
        this.bookId = bookId;
    }
}

