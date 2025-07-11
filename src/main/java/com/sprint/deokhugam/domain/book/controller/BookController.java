package com.sprint.deokhugam.domain.book.controller;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.service.BookService;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "도서 관리", description = "도서 관련 API")
public class BookController {

    private final BookService bookService;

    @Operation(summary = "도서 목록 조회", description = "키워드 검색과 커서 페이지네이션을 통한 도서 목록 조회")
    @GetMapping
    public ResponseEntity<CursorPageResponse<BookDto>> getBooks(
        @Parameter(description = "검색 키워드 (제목, 저자, ISBN)")
        @RequestParam(required = false) String keyword,

        @Parameter(description = "정렬 기준 (title, publishedDate, rating, reviewCount)")
        @RequestParam(defaultValue = "title") String orderBy,

        @Parameter(description = "정렬 방향 (ASC, DESC)")
        @RequestParam(defaultValue = "DESC") String direction,

        @Parameter(description = "커서 값")
        @RequestParam(required = false) String cursor,

        @Parameter(description = "이전 페이지 마지막 요소의 생성 시간 (밀리초)")
        @RequestParam(required = false) Long after,

        @Parameter(description = "페이지 크기 (1-100)")
        @RequestParam(defaultValue = "50") Integer limit
    ) {
        log.info("도서 목록 조회 요청 - keyword: {}, orderBy: {}, direction: {}, cursor: {}, after: {}, limit: {}",
            keyword, orderBy, direction, cursor, after, limit);

        // Request DTO 생성
        BookSearchRequest request = BookSearchRequest.of(
            keyword,
            orderBy,
            direction,
            cursor,
            after != null ? Instant.ofEpochMilli(after) : null,
            limit
        );

        CursorPageResponse<BookDto> response = bookService.getBooks(request);

        return ResponseEntity.ok(response);
    }
}

