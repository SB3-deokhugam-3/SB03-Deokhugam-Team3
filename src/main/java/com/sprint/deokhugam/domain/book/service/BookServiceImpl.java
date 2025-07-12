package com.sprint.deokhugam.domain.book.service;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.mapper.BookMapper;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.user.storage.s3.S3Storage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final S3Storage s3Storage;

    @Override
    public BookDto create(BookCreateRequest bookData, MultipartFile thumbnailImage) throws IOException {
        log.debug("[BookService]: 책 등록 요청 - bookData: {}", bookData);

        String isbn = bookData.isbn();

        if (bookRepository.existsByIsbn(isbn)) {

        }

        Book book = bookMapper.toEntity(bookData);

        if (thumbnailImage != null) {
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
}
