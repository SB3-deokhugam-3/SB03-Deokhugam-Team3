package com.sprint.deokhugam.domain.book.service;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.exception.BookNotFoundException;
import com.sprint.deokhugam.domain.book.exception.DuplicateIsbnException;
import com.sprint.deokhugam.domain.book.mapper.BookMapper;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.book.storage.s3.S3Storage;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
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
    @Transactional
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

        return bookMapper.toDto(savedBook, s3Storage);
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
            books.stream().map(book -> bookMapper.toDto(book, s3Storage)).toList(),
            nextCursor,
            nextAfter,
            books.size(),
            totalElements,
            hasNext
        );

        log.info("도서 목록 조회 완료 - 결과 수: {}, 다음 페이지 존재: {}", response.size(), response.hasNext());

        return response;
    }

    public BookDto findById(UUID bookId) {
        log.debug("[BookService] 책 상세 정보 조회 요청 - id: {}", bookId);

        Book book = findBook(bookId);

        log.info("책 조회 성공: book: {}", book);

        return bookMapper.toDto(book, s3Storage);
    }

    @Override
    @Transactional
    public BookDto update(UUID bookId, BookUpdateRequest bookData, MultipartFile thumbnailImage) throws IOException {
        log.debug("[BookService] 책 정보 수정 요청 - id: {}", bookId);

        Book book = findBook(bookId);

        book.updateTitle(bookData.title());
        book.updateAuthor(bookData.author());
        book.updateDescription(bookData.description());
        book.updatePublisher(bookData.publisher());
        book.updatePublishedDate(bookData.publishedDate());

        // 기존 썸네일이 있는 경우 기존 썸네일 S3에서 삭제
        if (book.getThumbnailUrl() != null) {
            s3Storage.deleteImage(book.getThumbnailUrl());
        }

        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            // Book Entity를 저장할 때는 S3의 실제 경로 저장
            String thumbnailImageUrl = s3Storage.uploadImage(thumbnailImage);
            book.updateThumbnailUrl(thumbnailImageUrl);
        } else {
            book.updateThumbnailUrl(null);
        }

        Book updatedBook = bookRepository.save(book);

        log.info("[BookService] 도서 정보 수정 완료- book: {}" , updatedBook);

        return bookMapper.toDto(updatedBook, s3Storage);
    }

    private Book findBook(UUID bookId) {
        return bookRepository.findById(bookId)
            .orElseThrow(() -> {
                log.warn("[BookService]: 도서 조회 실패: id: {}", bookId);
                return new BookNotFoundException(bookId);
            });
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
