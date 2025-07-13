package com.sprint.deokhugam.domain.book.service;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.exception.DuplicateIsbnException;
import com.sprint.deokhugam.domain.book.mapper.BookMapper;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.user.storage.s3.S3Storage;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final S3Storage s3Storage;

    @Override
    public BookDto create(BookCreateRequest bookData, MultipartFile thumbnailImage) throws IOException {
        log.debug("[BookService]: 책 등록 요청 - bookData: {}", bookData);

        String isbn = bookData.isbn();

        if (bookRepository.existsByIsbn(isbn)) {
            throw new DuplicateIsbnException(isbn);
        }

        Book book = bookMapper.toEntity(bookData);

        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            // Book Entity를 저장할 때는 S3의 실제 경로 저장
            String thumbnailImageUrl = s3Storage.uploadImage(thumbnailImage);
            book.updateThumbnailUrl(thumbnailImageUrl);
        }

        Book savedBook = bookRepository.save(book);

        log.info("책 등록 완료: id={}, title={}", savedBook.getId(), savedBook.getTitle());

        if (savedBook.getThumbnailUrl() != null) {
            String presignedUrl = s3Storage.generatePresignedUrl(savedBook.getThumbnailUrl());

            return BookDto.builder()
                .id(savedBook.getId())
                .createdAt(savedBook.getCreatedAt())
                .updatedAt(savedBook.getUpdatedAt())
                .title(savedBook.getTitle())
                .author(savedBook.getAuthor())
                .description(savedBook.getDescription())
                .publisher(savedBook.getPublisher())
                .publishedDate(savedBook.getPublishedDate())
                .isbn(savedBook.getIsbn())
                .thumbnailUrl(presignedUrl)
                .rating(savedBook.getRating())
                .reviewCount(savedBook.getReviewCount())
                .build();
        }

        return bookMapper.toDto(savedBook);
    }
    @Override
    public CursorPageResponse<BookDto> getBooks(BookSearchRequest request) {
        log.info("도서 목록 조회 시작 - request: {}", request);

        // 유효성 검증
        BookSearchRequest validatedRequest = request.validate();

        // 하나 더 조회하여 다음 페이지 존재 여부 확인
        BookSearchRequest queryRequest = BookSearchRequest.of(
            validatedRequest.keyword(),
            validatedRequest.orderBy(),
            validatedRequest.direction(),
            validatedRequest.cursor(),
            validatedRequest.after(),
            validatedRequest.limit() + 1
        );

        List<Book> books;

        // 커서 기반 페이지네이션
        if (validatedRequest.hasCursor()) {
            books = bookRepository.findBooksWithKeywordAndCursor(queryRequest);
        } else {
            books = bookRepository.findBooksWithKeyword(queryRequest);
        }

        // 총 개수 조회
        long totalElements = bookRepository.countBooksWithKeyword(validatedRequest.keyword());

        // 다음 페이지 존재 여부 확인
        boolean hasNext = books.size() > validatedRequest.limit();
        if (hasNext) {
            books = books.subList(0, validatedRequest.limit());
        }

        // Next 커서 생성
        String nextCursor = null;
        String nextAfter = null;

        if (hasNext && !books.isEmpty()) {
            Book lastBook = books.get(books.size() - 1);
            nextCursor = getCursorValue(lastBook, validatedRequest.orderBy());
            nextAfter = lastBook.getCreatedAt().toString();
        }

        CursorPageResponse<BookDto> response = new CursorPageResponse<>(
            books.stream().map(bookMapper::toBookDto).toList(),
            nextCursor,
            nextAfter,
            books.size(),
            totalElements,
            hasNext
        );

        log.info("도서 목록 조회 완료 - 결과 수: {}, 다음 페이지 존재: {}", response.size(), response.hasNext());

        return response;
    }

    private String getCursorValue(Book book, String orderBy) {
        return switch (orderBy) {
            case "title" -> book.getTitle();
            case "publishedDate" -> book.getPublishedDate().toString();
            case "rating" -> book.getRating().toString();
            case "reviewCount" -> book.getReviewCount().toString();
            default -> book.getTitle();
        };
    }
}
