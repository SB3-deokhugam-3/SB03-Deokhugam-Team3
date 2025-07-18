package com.sprint.deokhugam.domain.book.service;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.exception.BookNotFoundException;
import com.sprint.deokhugam.domain.book.exception.BookNotSoftDeletedException;
import com.sprint.deokhugam.domain.book.exception.DuplicateIsbnException;
import com.sprint.deokhugam.domain.book.exception.FileSizeExceededException;
import com.sprint.deokhugam.domain.book.exception.InvalidFileTypeException;
import com.sprint.deokhugam.domain.book.exception.OcrException;
import com.sprint.deokhugam.domain.book.mapper.BookMapper;
import com.sprint.deokhugam.domain.book.ocr.TesseractOcrExtractor;
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
    private final TesseractOcrExtractor tesseractOcrExtractor;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> SUPPORTED_IMAGE_TYPES =
        List.of("image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp");

    @Override
    @Transactional
    public BookDto create(BookCreateRequest bookData, MultipartFile thumbnailImage) throws IOException {
        log.debug("[BookService] 책 등록 요청 - bookData: {}", bookData);

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

        log.info("[BookService] 책 등록 완료: id={}, title={}", savedBook.getId(), savedBook.getTitle());

        return bookMapper.toDto(savedBook, s3Storage);
    }

    @Override
    public CursorPageResponse<BookDto> getBooks(BookSearchRequest request) {
        log.info("[BookService] 도서 목록 조회 시작 - request: {}", request);

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

        log.info("[BookService] 도서 목록 조회 완료 - 결과 수: {}, 다음 페이지 존재: {}", response.size(), response.hasNext());

        return response;
    }

    @Override
    public String extractIsbnFromImage(MultipartFile imageFile) throws OcrException {
        log.info("[BookService] 이미지에서 ISBN 추출 요청 - 파일명: {}, 크기: {} bytes",
            imageFile.getOriginalFilename(), imageFile.getSize());

        // 파일 유효성 검사
        validateImageFile(imageFile);

        try {
            // OCR 서비스 사용 가능 여부 확인
            if (!tesseractOcrExtractor.isAvailable()) {
                throw OcrException.serverError("OCR 서버 내부 오류가 발생했습니다.");
            }

            // OCR 처리
            String extractedIsbn = tesseractOcrExtractor.extractIsbn(imageFile);

            // OCR 결과 검증
            if (extractedIsbn == null || extractedIsbn.trim().isEmpty()) {
                throw OcrException.serverError("OCR 서버 내부 오류가 발생했습니다.");
            }

            return extractedIsbn;
        } catch (OcrException e) {
            // 이미 OcrException인 경우 그대로 전파
            throw e;
        } catch (RuntimeException e) {
            log.error("[BookService] OCR 처리 중 런타임 예외 발생", e);
            throw OcrException.serverError("OCR 서버 내부 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("[BookService] OCR 처리 중 예상치 못한 예외 발생", e);
            throw OcrException.serverError("OCR 서버 내부 오류가 발생했습니다.", e);
        }
    }

    /**
     * 이미지 파일 유효성 검사
     * */

    private void validateImageFile(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("[BookService] 이미지 파일이 필요합니다.");
        }

        if (image.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeExceededException(image.getSize(), MAX_FILE_SIZE);
        }

        // 파일 형식 검사
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException("[BookService] 이미지 파일만 업로드 가능합니다.");
        }

        // 지원되는 이미지 형식 검사
        if (!SUPPORTED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("[BookService] 지원되지 않는 이미지 형식입니다. ( 지원 형식 : JPEG, PNG, GIF, BMP, WEBP )");
        }
    }

    public BookDto findById(UUID bookId) {
        log.debug("[BookService] 책 상세 정보 조회 요청 - id: {}", bookId);

        Book book = findBook(bookId);

        log.info("[BookService] 책 조회 성공: book: {}", book);

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

        if (thumbnailImage != null) {
            if (thumbnailImage.isEmpty()) {
                // thumbnailImage 파트가 빈 파일 -> 이미지 삭제 요청으로 해석
                if (book.getThumbnailUrl() != null) {
                    s3Storage.deleteImage(book.getThumbnailUrl());
                    book.updateThumbnailUrl(null);
                    log.info("[BookService] 썸네일 삭제 완료 - id: {}", bookId);
                }
            } else {
                // thumbnailImage에 새 파일이 들어있음 -> 기존 삭제 후 새 업로드
                if (book.getThumbnailUrl() != null) {
                    s3Storage.deleteImage(book.getThumbnailUrl());
                }
                String thumbnailImageUrl = s3Storage.uploadImage(thumbnailImage);
                book.updateThumbnailUrl(thumbnailImageUrl);
                log.info("[BookService] 썸네일 새로 업로드 완료 - id: {}", bookId);
            }
        }

        Book updatedBook = bookRepository.save(book);

        log.info("[BookService] 도서 정보 수정 완료- book: {}" , updatedBook);

        return bookMapper.toDto(updatedBook, s3Storage);
    }

    @Override
    @Transactional
    public void delete(UUID bookId) {
        log.info("[BookService] 도서 논리 삭제 요청 - id: {}", bookId);

        Book book = findBook(bookId);


        book.delete();
        bookRepository.save(book);

        log.info("[BookService] 도서 논리 삭제 완료 - id: {}", bookId);
    }

    @Override
    @Transactional
    public void hardDelete(UUID bookId) {
        log.info("[BookService] 도서 물리 삭제 요청 - id: {}", bookId);

        // 삭제 여부 확인 및 검증
        Book book = bookRepository.findByIdIncludingDeleted(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));

        if (!book.isDeleted()) { // 논리 삭제되지 않은 경우
            log.error("[BookService] 물리 삭제 실패 - 논리 삭제되지 않은 도서입니다. id: {}", bookId);
            throw new BookNotSoftDeletedException(bookId);
        }

        // S3에서 썸네일 이미지 삭제
        if (book.getThumbnailUrl() != null) {
            try {
                s3Storage.deleteImage(book.getThumbnailUrl());
                log.info("[BookService] 썸네일 이미지 삭제 완료 - id: {}", bookId);
            } catch (Exception e) {
                log.error("[BookService] 썸네일 이미지 삭제 실패 - id: {}, error: {}", bookId, e.getMessage());
            }
        }

        // 데이터베이스에서 물리 삭제 (관련 데이터 모두 삭제)
        bookRepository.hardDeleteBook(bookId);
        log.info("[BookService] 도서 물리 삭제 완료 - id: {}", bookId);
    }


    private Book findBook(UUID bookId) {
        return bookRepository.findById(bookId)
            .orElseThrow(() -> {
                log.warn("[BookService] 도서 조회 실패: id: {}", bookId);
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
