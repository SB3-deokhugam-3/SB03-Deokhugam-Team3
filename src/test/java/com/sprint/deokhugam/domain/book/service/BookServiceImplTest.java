package com.sprint.deokhugam.domain.book.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.mapper.BookMapper;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.user.storage.s3.S3Storage;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private S3Storage storage;

    @Test
    void 책을_등록하면_책이_저장된다() throws IOException {

        // given
        UUID bookId = UUID.randomUUID();
        BookCreateRequest request = BookCreateRequest.builder()
            .title("test book")
            .author("test author")
            .description("test description")
            .publisher("test publisher")
            .publishedDate(LocalDate.now())
            .isbn("1234567890123")
            .build();

        MultipartFile thumbnail = new MockMultipartFile(
            "coverImage",
            "cover.png",
            "image/png",
            "dummy image data".getBytes()
        );

        Book bookEntity =  Book.builder()
            .title("test book")
            .author("test author")
            .description("test description")
            .publisher("test publisher")
            .publishedDate(LocalDate.now())
            .isbn("1234567890123")
            .rating(0.0)
            .reviewCount(0L)
            .isDeleted(false)
            .build();

        String thumbnailUrl = "http://test-url.com/cover.png";

        Book savedBook = Book.builder()
            .title("test book")
            .author("test author")
            .description("test description")
            .publisher("test publisher")
            .publishedDate(LocalDate.now())
            .isbn("1234567890123")
            .rating(0.0)
            .reviewCount(0L)
            .isDeleted(false)
            .thumbnailUrl(thumbnailUrl)
            .build();

        ReflectionTestUtils.setField(savedBook, "id", bookId);

        String presignedUrl = "https://cdn.example.com/cover.png";

        given(bookRepository.existsByIsbn(anyString())).willReturn(false);
        given(bookMapper.toEntity(request)).willReturn(bookEntity);
        given(storage.uploadImage(thumbnail)).willReturn(thumbnailUrl);
        given(bookRepository.save(bookEntity)).willReturn(savedBook);
        given(storage.generatePresignedUrl(thumbnailUrl)).willReturn(presignedUrl);

        // when
        BookDto result = bookService.create(request, thumbnail);

        // then
        assertNotNull(result);
        assertEquals(bookId, result.id());
        assertEquals("test book", result.title());
        assertEquals("test author", result.author());
        assertEquals("test description", result.description());
        assertEquals("test publisher", result.publisher());
        assertEquals(LocalDate.now(), result.publishedDate());
        assertEquals("1234567890123", result.isbn());
        assertEquals(presignedUrl, result.thumbnailUrl());
        assertEquals(0.0, result.rating());
        assertEquals(0L, result.reviewCount());
        verify(bookRepository).existsByIsbn("1234567890123");
        verify(bookMapper).toEntity(request);
        verify(storage).uploadImage(thumbnail);
        verify(bookRepository).save(any(Book.class));
        verify(storage).generatePresignedUrl(thumbnailUrl);
    }
}