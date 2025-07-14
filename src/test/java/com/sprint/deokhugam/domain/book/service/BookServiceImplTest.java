package com.sprint.deokhugam.domain.book.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.not;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.exception.BookNotFoundException;
import com.sprint.deokhugam.domain.book.exception.DuplicateIsbnException;
import com.sprint.deokhugam.domain.book.mapper.BookMapper;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.book.storage.s3.S3Storage;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
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

    private List<Book> testBooks;
    private List<BookDto> testBookDtos;
    private String title;
    private String author;
    private String description;
    private String publisher;
    private LocalDate publishedDate;
    private String isbn;

    @BeforeEach
    void setUp() {
        title = "test book";
        author = "test author";
        description = "test description";
        publisher = "test publisher";
        publishedDate = LocalDate.now();
        isbn = "1234567890123";

        testBooks = createTestBooks();
        testBookDtos = createTestBookDtos();
    }

    @Test
    void 썸네일_이미지가_있는_책을_등록하면_책이_저장된다() throws IOException {

        // given
        UUID bookId = UUID.randomUUID();
        BookCreateRequest request = createRequest(title, author, description, publisher, publishedDate,
            isbn);

        MultipartFile thumbnail = new MockMultipartFile(
            "coverImage",
            "cover.png",
            "image/png",
            "dummy image data".getBytes()
        );

        Book bookEntity = createBookEntity(title, author, description, publisher, publishedDate,
            isbn, null, 0.0, 0L);

        String thumbnailUrl = "http://test-url.com/cover.png";

        Book savedBook = createBookEntity(title, author, description, publisher, publishedDate,
            isbn, thumbnailUrl, 0.0, 0L);

        ReflectionTestUtils.setField(savedBook, "id", bookId);

        String presignedUrl = "https://cdn.example.com/cover.png";

        BookDto expectedResponse = createBookDto(bookId, title, author, description, publisher, publishedDate,
            isbn, presignedUrl, 0L, 0.0, Instant.now(), Instant.now());

        given(bookRepository.existsByIsbn(anyString())).willReturn(false);
        given(bookMapper.toEntity(request)).willReturn(bookEntity);
        given(storage.uploadImage(thumbnail)).willReturn(thumbnailUrl);
        given(bookRepository.save(bookEntity)).willReturn(savedBook);
        given(bookMapper.toDto(eq(savedBook), any(S3Storage.class))).willReturn(expectedResponse);

        // when
        BookDto result = bookService.create(request, thumbnail);

        // then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(bookRepository).existsByIsbn("1234567890123");
        verify(bookMapper).toEntity(request);
        verify(storage).uploadImage(thumbnail);
        verify(bookRepository).save(any(Book.class));
        verify(bookMapper).toDto(savedBook, storage);
    }

    @Test
    void 썸네일_이미지가_없는_책을_등록하면_책이_저장된다() throws IOException {

        // given
        UUID bookId = UUID.randomUUID();
        BookCreateRequest request = createRequest(title, author, description, publisher,
            publishedDate, isbn);

        Book bookEntity =  createBookEntity(title, author, description, publisher, publishedDate,
            isbn, null, 0.0, 0L);

        Book savedBook = createBookEntity(title, author, description, publisher, publishedDate,
            isbn, null, 0.0, 0L);

        ReflectionTestUtils.setField(savedBook, "id", bookId);

        BookDto expectedResponse = createBookDto(bookId, title, author, description, publisher, publishedDate,
            isbn, null, 0L, 0.0, Instant.now(), Instant.now());

        given(bookRepository.existsByIsbn(anyString())).willReturn(false);
        given(bookMapper.toEntity(request)).willReturn(bookEntity);
        given(bookRepository.save(bookEntity)).willReturn(savedBook);
        given(bookMapper.toDto(eq(savedBook), any(S3Storage.class))).willReturn(expectedResponse);

        // when
        BookDto result = bookService.create(request, null);

        // then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(bookRepository).existsByIsbn("1234567890123");
        verify(bookMapper).toEntity(request);
        verify(bookMapper).toDto(savedBook, storage);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void 중복_isbn으로_책_등록_시도_시_책_등록이_실패한다() {

        // given
        String existsIsbn = "1234567890123";

        BookCreateRequest request = createRequest(title, author, description, publisher, publishedDate,
            existsIsbn);

        given(bookRepository.existsByIsbn(existsIsbn)).willReturn(true);

        // when
        Throwable thrown = catchThrowable(() -> bookService.create(request, null));

        // then
        assertThat(thrown)
            .isInstanceOf(DuplicateIsbnException.class)
            .hasMessageContaining("존재");
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("첫 페이지 조회 - 키워드 검색 없이")
    void 키워드_없이_첫_페이지_조회() {
        //given
        BookSearchRequest request = BookSearchRequest.of(null, "title", "DESC",null,null,10);
        given(bookRepository.findBooksWithKeyword(any(BookSearchRequest.class)))
            .willReturn(testBooks.subList(0, 3));
        given(bookRepository.countBooksWithKeyword(any()))
            .willReturn(15L);
        given(bookMapper.toDto(any(Book.class), any(S3Storage.class)))
            .willReturn(testBookDtos.get(0), testBookDtos.get(1), testBookDtos.get(2));

        //when
        CursorPageResponse<BookDto> response = bookService.getBooks(request);

        //then
        Assertions.assertThat(response.content()).hasSize(3);
        Assertions.assertThat(response.totalElements()).isEqualTo(15L);
        Assertions.assertThat(response.hasNext()).isFalse();
        Assertions.assertThat(response.nextCursor()).isNull();
        Assertions.assertThat(response.nextAfter()).isNull();
        verify(bookRepository).findBooksWithKeyword(any(BookSearchRequest.class));
        verify(bookRepository).countBooksWithKeyword(any());
    }

    @Test
    @DisplayName("첫 페이지 조회 - 키워드 검색 포함")
    void 키워드_포함_첫_페이지_조회() {
        // given
        BookSearchRequest request = BookSearchRequest.of("Hot", "title", "DESC", null, null, 5);
        List<Book> searchResults = testBooks.subList(0, 3);
        given(bookRepository.findBooksWithKeyword(any(BookSearchRequest.class)))
            .willReturn(searchResults);
        given(bookRepository.countBooksWithKeyword("Hot"))
            .willReturn(3L);
        given(bookMapper.toDto(any(Book.class), any(S3Storage.class)))
            .willReturn(testBookDtos.get(0), testBookDtos.get(1), testBookDtos.get(2));

        // when
        CursorPageResponse<BookDto> response = bookService.getBooks(request);

        // then
        Assertions.assertThat(response.content()).hasSize(3);
        Assertions.assertThat(response.totalElements()).isEqualTo(3L);
        Assertions.assertThat(response.hasNext()).isFalse();
        verify(bookRepository).findBooksWithKeyword(any(BookSearchRequest.class));
        verify(bookRepository).countBooksWithKeyword("Hot");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - 다음 페이지 존재")
    void 커서_기반_페이지네이션_다음_페이지_존재() {
        // given
        Instant after = Instant.now().minusSeconds(3600);
        BookSearchRequest request = BookSearchRequest.of("Super", "title", "DESC", "Java Programming", after, 2);
        List<Book> searchResults = testBooks.subList(0, 3); // limit+1 = 3개 조회
        given(bookRepository.findBooksWithKeywordAndCursor(any(BookSearchRequest.class)))
            .willReturn(searchResults);
        given(bookRepository.countBooksWithKeyword("Super"))
            .willReturn(5L);
        given(bookMapper.toDto(any(Book.class), any(S3Storage.class)))
            .willReturn(testBookDtos.get(0), testBookDtos.get(1));

        // when
        CursorPageResponse<BookDto> response = bookService.getBooks(request);

        // then
        Assertions.assertThat(response.content()).hasSize(2);
        Assertions.assertThat(response.totalElements()).isEqualTo(5L);
        Assertions.assertThat(response.hasNext()).isTrue();
        Assertions.assertThat(response.nextCursor()).isNotNull();
        Assertions.assertThat(response.nextAfter()).isNotNull();
        verify(bookRepository).findBooksWithKeywordAndCursor(any(BookSearchRequest.class));
    }

    @Test
    @DisplayName("다양한 정렬 기준 테스트 - 평점순")
    void 평점순_정렬_테스트() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "rating", "DESC", null, null, 5);
        given(bookRepository.findBooksWithKeyword(any(BookSearchRequest.class)))
            .willReturn(testBooks);
        given(bookRepository.countBooksWithKeyword(any()))
            .willReturn(3L);
        given(bookMapper.toDto(any(Book.class), any(S3Storage.class)))
            .willReturn(testBookDtos.get(0), testBookDtos.get(1), testBookDtos.get(2));

        // when
        CursorPageResponse<BookDto> response = bookService.getBooks(request);

        // then
        Assertions.assertThat(response.content()).hasSize(3);
        verify(bookRepository).findBooksWithKeyword(any(BookSearchRequest.class));
    }

    @Test
    @DisplayName("유효하지 않은 정렬 기준 - 예외 발생")
    void 유효하지_않은_정렬_기준_예외_발생() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "invalid", "DESC", null, null, 5);

        // when & then
        assertThatThrownBy(() -> bookService.getBooks(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("정렬 기준은 title, publishedDate, rating, reviewCount 중 하나여야 합니다.");
    }

    @Test
    @DisplayName("유효하지 않은 페이지 크기 - 예외 발생")
    void 유효하지_않은_페이지_크기_예외_발생() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "title", "DESC", null, null, 101);

        // when & then
        assertThatThrownBy(() -> bookService.getBooks(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("페이지 크기는 1 이상 100 이하여야 합니다.");
    }

    @Test
    void 존재하는_도서_ID로_조회하면_도서_정보를_반환한다() {

        // given
        UUID bookId = UUID.randomUUID();
        Book book = createBookEntity(title, author, description, publisher, publishedDate, isbn,
            "http://test-url.com/cover.png", 0.0, 0L);

        String presignedUrl = "https://cdn.example.com/cover.png";

        BookDto expectedResponse = createBookDto(bookId, title, author, description, publisher, publishedDate,
            isbn, presignedUrl, 0L, 0.0, Instant.now(), Instant.now());

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(bookMapper.toDto(eq(book), any(S3Storage.class))).willReturn(expectedResponse);

        // when
        BookDto result = bookService.findById(bookId);

        // then_
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(bookRepository).findById(bookId);
        verify(bookMapper).toDto(book, storage);
    }

    @Test
    void 존재하지_않는_도서_ID로_조회하면_BookNotFoundException이_발생한다() {

        // given
        UUID notExistId = UUID.randomUUID();
        given(bookRepository.findById(notExistId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> bookService.findById(notExistId));

        // then
        assertThat(thrown)
            .isInstanceOf(BookNotFoundException.class)
            .hasMessageContaining("BOOK");
        verify(bookRepository).findById(notExistId);
    }

    private List<Book> createTestBooks() {
        Instant now = Instant.now();

        // 첫 번째 Book 생성 및 createdAt 설정
        Book book1 = createBookEntity("Super Hot Day", "김현기", "덥다 더워 덥다 더워",
            "3팀", LocalDate.of(2023, 1, 1), "9788123456789", null,
            4.5, 100L);

        // ReflectionTestUtils를 사용하여 BaseEntity의 필드 설정
        ReflectionTestUtils.setField(book1, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(book1, "createdAt", now.minusSeconds(3600));
        ReflectionTestUtils.setField(book1, "updatedAt", now.minusSeconds(1800));

        // 두 번째 Book 생성 및 createdAt 설정
        Book book2 = createBookEntity("Spring Boot Guide", "박스프링", "스프링 부트 가이드",
            "웹출판사", LocalDate.of(2023, 6, 1), "9788987654321", null,
            4.8, 200L);

        ReflectionTestUtils.setField(book2, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(book2, "createdAt", now.minusSeconds(7200));
        ReflectionTestUtils.setField(book2, "updatedAt", now.minusSeconds(3600));

        // 세 번째 Book 생성 및 createdAt 설정
        Book book3 = createBookEntity("Database Desing", "이데이터", "데이터베이스 설계",
            "DB출판사", LocalDate.of(2023, 3, 1), "9788555666777", null,
            4.2, 50L);

        ReflectionTestUtils.setField(book3, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(book3, "createdAt", now.minusSeconds(10800));
        ReflectionTestUtils.setField(book3, "updatedAt", now.minusSeconds(5400));

        return List.of(book1, book2, book3);
    }

    private List<BookDto> createTestBookDtos() {
        Instant now = Instant.now();
        return List.of(
            createBookDto(UUID.randomUUID(), "Super Hot Day", "김현기", "덥다 더워 덥다 더워",
                "3팀", LocalDate.of(2023, 1, 1), "9788123456789",
                null, 100L, 4.5, now.minusSeconds(3600),
                now.minusSeconds(1800)),
            createBookDto(UUID.randomUUID(), "Spring Boot Guide", "박스프링", "스프링 부트 가이드",
                "웹출판사", LocalDate.of(2023, 6, 1), "9788987654321",
                null, 200L, 4.8, now.minusSeconds(7200),
                now.minusSeconds(3600)),
            createBookDto(UUID.randomUUID(), "Database Design", "이데이터", "데이터베이스 설계",
                "DB출판사", LocalDate.of(2023, 3, 1), "9788555666777",
                null, 50L, 4.2, now.minusSeconds(10800),
                now.minusSeconds(5400))
        );
    }

    private Book createBookEntity(String title, String author, String description, String publisher,
        LocalDate publishedDate, String isbn, String thumbnailUrl, Double rating, Long reviewCount) {
        return Book.builder()
            .title(title)
            .author(author)
            .description(description)
            .publisher(publisher)
            .publishedDate(publishedDate)
            .isbn(isbn)
            .thumbnailUrl(thumbnailUrl)
            .rating(rating)
            .reviewCount(reviewCount)
            .isDeleted(false)
            .build();
    }

    private BookCreateRequest createRequest(String title, String author, String description,
        String publisher, LocalDate publishedDate, String isbn) {
        return BookCreateRequest.builder()
            .title(title)
            .author(author)
            .description(description)
            .publisher(publisher)
            .publishedDate(publishedDate)
            .isbn(isbn)
            .build();
    }

    private BookDto createBookDto(UUID id, String title, String author, String description,
        String publisher, LocalDate publishedDate, String isbn, String thumbnailUrl, Long reviewCount,
        Double rating, Instant createdAt, Instant updatedAt) {
        return BookDto.builder()
            .id(id)
            .title(title)
            .author(author)
            .description(description)
            .publisher(publisher)
            .publishedDate(publishedDate)
            .isbn(isbn)
            .thumbnailUrl(thumbnailUrl)
            .reviewCount(reviewCount)
            .rating(rating)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
}