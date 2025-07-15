package com.sprint.deokhugam.domain.book.exception;

import com.sprint.deokhugam.global.exception.NotFoundException;
import java.util.Map;

public class BookInfoNotFoundException extends NotFoundException {

    public BookInfoNotFoundException(String isbn) {
        super("BOOK_INFO", Map.of("isbn", isbn));
    }
}
