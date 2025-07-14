package com.sprint.deokhugam.domain.book.service;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.exception.BookNotFoundException;
import com.sprint.deokhugam.domain.book.exception.DuplicateIsbnException;
import com.sprint.deokhugam.domain.book.exception.OcrException;
import com.sprint.deokhugam.domain.book.mapper.BookMapper;
import com.sprint.deokhugam.domain.book.ocr.OcrExtractor;
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
    private final List<OcrExtractor> ocrExtractors;

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

        log.info("[BookService]: 책 등록 완료: id={}, title={}", savedBook.getId(), savedBook.getTitle());

        return bookMapper.toDto(savedBook, s3Storage);
    }

    @Override
    public CursorPageResponse<BookDto> getBooks(BookSearchRequest request) {
        log.info("[BookService]: 도서 목록 조회 시작 - request: {}", request);

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

        log.info("[BookService]: 도서 목록 조회 완료 - 결과 수: {}, 다음 페이지 존재: {}", response.size(), response.hasNext());

        return response;
    }

    @Override
    public String extractIsbnFromImage(MultipartFile imageFile) throws OcrException {
        log.info("[BookService]: 이미지에서 ISBN 추출 요청 - 파일명 : {}, 크기: {} bytes", imageFile.getOriginalFilename(), imageFile.getSize());

        // 사용 가능한 OCR 구현체 찾기 ( 우선순위 순 )
        OcrExtractor selectedExtractor = selectAvailableOcrExtractor();

        if (selectedExtractor == null) {
            log.error("[BookService]: 사용 가능한 OCR 구현체가 없습니다. 구현체 목록: {}",
                ocrExtractors.stream().map(e -> e.getClass().getSimpleName()).toList());
            throw new OcrException("[BookService]: OCR 서비스를 사용할 수 없습니다. 설정을 확인해주세요.");

        }

        String extractorName = selectedExtractor.getClass().getSimpleName();
        log.info("[BookService]: 선택된 OCR 구현체 : {} ( 우선순위 : {} )", extractorName, selectedExtractor.getPriority());

        try {
            // OCR 실행
            String isbn = selectedExtractor.extractIsbn(imageFile);

            if (isbn == null || isbn.trim().isEmpty()) {
                log.warn("[BookService]: {}에서 ISBN을 추출할 수 없습니다.", extractorName);

                // 다른 OCR 구현체로 재시도
                return retryWithOtherExtractors(imageFile, selectedExtractor);
            }

            log.info("[BookService]: ISBN 추출 성공 : {} ( 사용된 OCR : {} )", isbn, extractorName);
            return isbn;
        } catch (Exception e) {
            log.error("[BookService]: {}에서 OCR 처리 실패", extractorName, e);

            // 다른 OCR 구현체로 재시도
            return retryWithOtherExtractors(imageFile, selectedExtractor);
        }
    }

    /**
     * 사용 가능한 OCR 구현체를 우선순위 순으로 선택
     *  */
    private OcrExtractor selectAvailableOcrExtractor() {
        return ocrExtractors.stream()
            .filter(OcrExtractor::isAvailable)
            .min((e1, e2) -> Integer.compare(e1.getPriority(), e2.getPriority()))
            .orElse(null);
    }

    /**
     *  실패한 OCR 구현체를 제외하고 다른 구현체로 재시도
     * */
    private String retryWithOtherExtractors(MultipartFile imageFile, OcrExtractor failedExtractor) throws OcrException {

        log.info("[BookService]: 다른 OCR 구현체로 재시도 시작");

        List<OcrExtractor> availableExtractors = ocrExtractors.stream()
            .filter(OcrExtractor::isAvailable)
            .filter(extractor -> !extractor.equals(failedExtractor))
            .sorted((e1,e2) -> Integer.compare(e1.getPriority(), e2.getPriority()))
            .toList();

        if (availableExtractors.isEmpty()) {
            log.error("[BookService]: 재시도할 수 있는 OCR 구현체가 없습니다.");
            throw new OcrException("[BookService]: 모든 OCR 구현체에서 ISBN 추출에 실패했습니다.");
        }

        for (OcrExtractor extractor : availableExtractors) {
            String extractorName = extractor.getClass().getSimpleName();
            log.info("[BookService]: OCR 재시도 : {} ( 우선순위 : {} )", extractorName, extractor.getPriority());

            try {
                String isbn = extractor.extractIsbn(imageFile);

                if (isbn != null && !isbn.trim().isEmpty()) {
                    log.info("[BookService]: 재시도 성공 : {} ( 사용된 OCR : {} )", isbn, extractorName);
                    return isbn;
                }

                log.warn("[BookService]: {}에서 ISBN을 추출할 수 없습니다.", extractorName);

            } catch (Exception e) {
                log.error("[BookService]: {}에서 OCR 처리 실패", extractorName, e);
                // 다음 구현체로 계속 진행
            }
        }

        // 모든 구현체에서 실패
        log.error("[BookService]: 모든 OCR 구현체에서 ISBN 추출에 실패했습니다.");
        throw new OcrException("[BookService]: 이미지에서 ISBN을 찾을 수 없습니다.");
    }

    public BookDto findById(UUID bookId) {
        log.debug("[BookService] 책 상세 정보 조회 요청 - id: {}", bookId);

        Book book = findBook(bookId);

        log.info("[BookService]: 책 조회 성공: book: {}", book);

        return bookMapper.toDto(book, s3Storage);
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
