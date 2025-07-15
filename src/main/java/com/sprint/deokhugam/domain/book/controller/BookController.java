package com.sprint.deokhugam.domain.book.controller;

import com.sprint.deokhugam.domain.api.BookInfoProvider;
import com.sprint.deokhugam.domain.api.dto.NaverBookDto;
import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.service.BookService;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;
    private final BookInfoProvider provider;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDto> create(
        @Valid @RequestPart("bookData") BookCreateRequest bookData,
        @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) throws IOException {
        BookDto result = bookService.create(bookData, thumbnailImage);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /*
     * 도서 목록 조회 ( 키워드 검색 + 커서 페이지네이션 )
     * @param keyword 검색 키워드 ( 제목, 저자, ISBN에서 부분 일치 )
     * @param orderBy 정렬 기준 ( 제목, 출판일, 평점, 리뷰수 )
     * @param direction 정렬 방향 ( ASC, DESC )
     * @param cursor 커서 값 ( 이전 페이지 마지막 요소의 정렬 기준 값 )
     * @param after 이전 페이지 마지막 요소의 생성 시간
     * @param limit 페이지 크기
     * @return 도서 목록 응답 */

    @GetMapping
    public ResponseEntity<CursorPageResponse<BookDto>> getBooks(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "title") String orderBy,
        @RequestParam(defaultValue = "DESC") String direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Long after,
        @RequestParam(defaultValue = "50") Integer limit
    ) {
        log.info(
            "도서 목록 조회 요청 - keyword: {}, orderBy: {}, direction: {}, cursor: {}, after: {}, limit: {}",
            keyword, orderBy, direction, cursor, after, limit);

        // Request DTO 생성
        BookSearchRequest request = BookSearchRequest.of(
            keyword,
            orderBy,
            direction,
            cursor,
            after != null ? Instant.ofEpochMilli(after) : null,
            limit
        ).validate();

        CursorPageResponse<BookDto> response = bookService.getBooks(request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDto> getBook(@PathVariable UUID bookId) {
        log.info("[BookController] 도서 상세 정보 요청 - id: {}", bookId);

        BookDto result = bookService.findById(bookId);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/info")
    public ResponseEntity<NaverBookDto> getBookInfoByIsbn(@RequestParam String isbn) {
        log.info("[BookController] 도서 정보 조회 요청 - isbn: {}", isbn);

        NaverBookDto result = provider.fetchInfoByIsbn(isbn);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
