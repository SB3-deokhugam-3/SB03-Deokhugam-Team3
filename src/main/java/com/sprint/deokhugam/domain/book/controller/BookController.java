package com.sprint.deokhugam.domain.book.controller;

import com.sprint.deokhugam.domain.api.BookInfoProvider;
import com.sprint.deokhugam.domain.api.dto.NaverBookDto;
import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sprint.deokhugam.domain.book.exception.OcrException;
import com.sprint.deokhugam.domain.book.service.BookService;
import com.sprint.deokhugam.domain.popularbook.dto.data.PopularBookDto;
import com.sprint.deokhugam.domain.popularbook.dto.request.PopularBookGetRequest;
import com.sprint.deokhugam.domain.popularbook.service.PopularBookService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
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
    private final PopularBookService popularBookService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookDto> create(
        @Valid @RequestPart("bookData") BookCreateRequest bookData,
        @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) throws IOException {
        BookDto result = bookService.create(bookData, thumbnailImage);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * 도서 목록 조회 ( 키워드 검색 + 커서 페이지네이션 )
     * @param keyword 검색 키워드 ( 제목, 저자, ISBN에서 부분 일치 )
     * @param orderBy 정렬 기준 ( 제목, 출판일, 평점, 리뷰수 )
     * @param direction 정렬 방향 ( ASC, DESC )
     * @param cursor 커서 값 ( 이전 페이지 마지막 요소의 정렬 기준 값 )
     * @param after 이전 페이지 마지막 요소의 생성 시간
     * @param limit 페이지 크기
     * @return 도서 목록 응답
     * */

    @GetMapping
    public ResponseEntity<CursorPageResponse<BookDto>> getBooks(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "title") String orderBy,
        @RequestParam(defaultValue = "DESC") String direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Long after,
        @RequestParam(defaultValue = "50") Integer limit
    ) {
        log.info("[BookController] 도서 목록 조회 요청 - keyword: {}, orderBy: {}, direction: {}, cursor: {}, after: {}, limit: {}",
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

    @PatchMapping("/{bookId}")
    public ResponseEntity<BookDto> update(
        @PathVariable UUID bookId,
        @Valid @RequestPart("bookData") BookUpdateRequest bookData,
        @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) throws IOException {
        log.info("[BookController] 도서 정보 수정 요청 - id: {}", bookId);

        BookDto result = bookService.update(bookId, bookData, thumbnailImage);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     * 이미지 기반 ISBN 인식
     * 도서 이미지를 통해 ISBN을 인식합니다.
     *
     * @param image 도서 이미지
     * @return 인식된 ISBN 문자열
     * @throws OcrException OCR 처리 중 오류 발생 시
     * */
    @PostMapping("/isbn/ocr")
    public ResponseEntity<String> extractIsbnFromImage(
        @RequestPart(value = "image") MultipartFile image
    ) throws OcrException {
        log.info("[BookController] ISBN 추출 요청 - 파일명 : {}, 크기 : {} bytes"
            , image.getOriginalFilename(), image.getSize());

        // OCR 서비스 호출
        String extractedIsbn = bookService.extractIsbnFromImage(image);

        log.info("ISBN 추출 완료 - ISBN : {}", extractedIsbn);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(extractedIsbn);
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> delete(@PathVariable UUID bookId) {
        log.info("[BookController] 도서 논리 삭제 요청 - id: {}", bookId);

        bookService.delete(bookId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{bookId}/hard")
    public ResponseEntity<Void> hardDelete(@PathVariable UUID bookId) {
        log.info("[BookController] 도서 물리 삭제 요청 - id: {}", bookId);
        bookService.hardDelete(bookId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<CursorPageResponse<PopularBookDto>> getPopularBooks(
        @ModelAttribute PopularBookGetRequest request
    ) {
        log.info("[BookController] 인기 도서 목록 조회 요청 - period: {}, limit: {}",
            request.period(), request.limit());

        CursorPageResponse<PopularBookDto> result = popularBookService.getPopularBooks(request);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}