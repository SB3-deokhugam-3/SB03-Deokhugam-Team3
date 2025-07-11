package com.sprint.deokhugam.domain.book.service;

import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

    void create(BookCreateRequest bookData, MultipartFile thumbnailImage);
}
