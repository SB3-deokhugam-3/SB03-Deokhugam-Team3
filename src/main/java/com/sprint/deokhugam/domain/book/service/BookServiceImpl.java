package com.sprint.deokhugam.domain.book.service;

import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BookServiceImpl implements BookService {

    @Override
    public void create(BookCreateRequest bookData, MultipartFile thumbnailImage) {

    }
}
