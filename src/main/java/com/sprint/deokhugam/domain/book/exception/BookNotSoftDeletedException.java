package com.sprint.deokhugam.domain.book.exception;

import java.util.UUID;

public class BookNotSoftDeletedException extends RuntimeException {

    public BookNotSoftDeletedException(UUID bookId) {
        super("논리 삭제되지 않은 도서입니다. ID : " + bookId);
    }
}
