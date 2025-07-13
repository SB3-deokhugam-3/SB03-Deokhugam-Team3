package com.sprint.deokhugam.domain.book.service;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;

public interface BookService {

    BookDto create(BookCreateRequest bookData, MultipartFile thumbnailImage) throws IOException;

    CursorPageResponse<BookDto> getBooks(BookSearchRequest request);
}
