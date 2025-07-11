package com.sprint.deokhugam.domain.book.service;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

    BookDto create(BookCreateRequest bookData, MultipartFile thumbnailImage);
}
