package com.sprint.deokhugam.domain.book.controller;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.exception.OcrException;
import com.sprint.deokhugam.domain.book.service.BookService;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
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
        log.info("[BookController]: 도서 목록 조회 요청 - keyword: {}, orderBy: {}, direction: {}, cursor: {}, after: {}, limit: {}",
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
        log.info("[BookController]: ISBN 추출 요청 - 파일명 : {}, 크기 : {} bytes", image.getOriginalFilename(), image.getSize());

        try {

            // 파일 유효성 검사
            validateImageFile(image);

            // OCR 서비스 호출
            String extractedIsbn = bookService.extractIsbnFromImage(image);

            log.info("ISBN 추출 완료 - ISBN : {}", extractedIsbn);

            return ResponseEntity.status(HttpStatus.OK).body(extractedIsbn);
        } catch (Exception e) {
            log.error("[BookController]: ISBN 추출 중 오류 발생",e);
            throw new OcrException("[BookController]: 이미지에서 ISBN을 추출하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 이미지 파일 유효성 검사
     * */
    private void validateImageFile(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("[BookController]: 이미지 파일이 필요합니다.");
        }

        // 파일 크기 검사 ( 10MB 제한 )
        long maxSize = 10 * 1024 * 1024;
        if (image.getSize() > maxSize) {
            throw new IllegalArgumentException("[BookController]: 파일 크기는 10MB를 초과할 수 없습니다.");
        }

        // 파일 형식 검사
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("[BookController]: 이미지 파일만 업로드 가능합니다.");
        }

        // 지원되는 이미 형식 검사
        List<String> supportedTypes = List.of("image/jpeg","image/jpg","image/png","image/gif","image/bmp","image/webp");
        if (!supportedTypes.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("[BookController]: 지원되지 않는 이미지 형식입니다. ( 지원 형식 : JPEG, PNG, GIF, BMP, WEBP )");
        }
    }
}