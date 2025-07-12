package com.sprint.deokhugam.domain.book.service;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;

public interface BookService {
    CursorPageResponse<BookDto> getBooks(BookSearchRequest request);
}
