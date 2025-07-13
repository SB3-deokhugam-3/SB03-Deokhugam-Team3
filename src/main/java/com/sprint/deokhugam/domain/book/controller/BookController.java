package com.sprint.deokhugam.domain.book.controller;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.service.BookService;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDto> create(
        @Valid @RequestPart("bookData") BookCreateRequest bookData,
        @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) throws IOException {
        BookDto result = bookService.create(bookData, thumbnailImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
