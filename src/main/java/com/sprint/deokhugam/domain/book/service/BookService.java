package com.sprint.deokhugam.domain.book.service;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sprint.deokhugam.domain.book.exception.OcrException;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

    BookDto create(BookCreateRequest bookData, MultipartFile thumbnailImage) throws IOException;

    CursorPageResponse<BookDto> getBooks(BookSearchRequest request);

    BookDto findById(UUID bookId);

    BookDto update(UUID bookId, BookUpdateRequest bookData, MultipartFile thumbnailImage)
        throws IOException;

    void delete(UUID bookId);

    void hardDelete(UUID bookId);

    /**
     * 이미지에서 ISBN을 추출
     *
     * @param imageFile 도서 이미지 파일
     * @return 추출된 ISBN 문자열
     * @throws OcrException OCR  처리 중 오류 발생시
     */
    String extractIsbnFromImage(MultipartFile imageFile) throws OcrException;
}
