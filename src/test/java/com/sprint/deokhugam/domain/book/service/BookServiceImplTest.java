package com.sprint.deokhugam.domain.book.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.exception.BookNotFoundException;
import com.sprint.deokhugam.domain.book.exception.BookNotSoftDeletedException;
import com.sprint.deokhugam.domain.book.exception.DuplicateIsbnException;
import com.sprint.deokhugam.domain.book.exception.OcrException;
import com.sprint.deokhugam.domain.book.mapper.BookMapper;
import com.sprint.deokhugam.domain.book.ocr.TesseractOcrExtractor;
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
@DisplayName("BookServiceImpl테스트")
class BookServiceImplTest {

    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private S3Storage storage;

    @Mock
    TesseractOcrExtractor tesseractOcrExtractor;


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
    void 유효하지_않은_정렬_기준_예외_발생() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "invalid", "DESC", null, null, 5);

        // when & then
        assertThatThrownBy(() -> bookService.getBooks(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("정렬 기준은 title, publishedDate, rating, reviewCount 중 하나여야 합니다.");
    }

    @Test
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

    @Test
    void 썸네일이_있는_도서를_수정하면_수정된_책_정보를_반환한다() throws IOException {

        // given
        UUID bookId = UUID.randomUUID();

        Book existBook = createBookEntity(title, author, description, publisher, publishedDate,
            isbn, "testImage.com", 0.0, 0L);
        BookUpdateRequest updateRequest = createUpdateRequest("new title", "new author",
            "new description", "new publisher", LocalDate.of(2023, 3, 3));

        MultipartFile newThumbnail = new MockMultipartFile(
            "coverImage",
            "newCover.png",
            "image/png",
            "dummy image data".getBytes()
        );

        Book updatedBook = createBookEntity("new title", "new author", "new description",
            "new publisher", LocalDate.of(2023, 3, 3), isbn, "newCover.com",
            0.0, 0L);

        String presignedUrl = "https://cdn.example.com/cover.png";

        BookDto expectedResponse = createBookDto(bookId, "new title", "new author", "new description",
            "new publisher", LocalDate.of(2023, 3, 3), isbn, presignedUrl, 0L, 0.0,
            Instant.now(), Instant.now());

        ReflectionTestUtils.setField(existBook, "id", bookId);
        ReflectionTestUtils.setField(updatedBook, "id", bookId);

        given(bookRepository.findById(bookId)).willReturn(Optional.of(existBook));
        given(storage.uploadImage(newThumbnail)).willReturn("newCover.com");
        given(bookRepository.save(any(Book.class))).willReturn(updatedBook);
        given(bookMapper.toDto(eq(updatedBook), any(S3Storage.class))).willReturn(expectedResponse);

        // when
        BookDto result = bookService.update(bookId, updateRequest, newThumbnail);

        // then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(storage).uploadImage(newThumbnail);
        verify(storage).deleteImage("testImage.com");
        verify(bookRepository).save(any(Book.class));
        verify(bookMapper).toDto(updatedBook, storage);
    }

    @Test
    void 썸네일이_없는_도서를_수정하면_수정된_책_정보를_반환한다() throws IOException {

        // given
        UUID bookId = UUID.randomUUID();

        Book existBook = createBookEntity(title, author, description, publisher, publishedDate,
            isbn, "testImage.com", 0.0, 0L);
        BookUpdateRequest updateRequest = createUpdateRequest("new title", "new author",
            "new description", "new publisher", LocalDate.of(1999, 7, 2));

        Book updatedBook = createBookEntity("new title", "new author", "new description",
            "new publisher", LocalDate.of(1999, 7, 2), isbn, null,
            0.0, 0L);

        BookDto expectedResponse = createBookDto(bookId, "new title", "new author", "new description",
            "new publisher", LocalDate.of(1999, 7, 2), isbn, null, 0L, 0.0,
            Instant.now(), Instant.now());

        ReflectionTestUtils.setField(existBook, "id", bookId);
        ReflectionTestUtils.setField(updatedBook, "id", bookId);

        given(bookRepository.findById(bookId)).willReturn(Optional.of(existBook));
        given(bookRepository.save(any(Book.class))).willReturn(updatedBook);
        given(bookMapper.toDto(eq(updatedBook), any(S3Storage.class))).willReturn(expectedResponse);

        // when
        BookDto result = bookService.update(bookId, updateRequest, null);

        // then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(bookRepository).save(any(Book.class));
        verify(bookMapper).toDto(updatedBook, storage);
    }

    @Test
    void 썸네일이_없는_도서의_썸네일을_수정하면_삭제없이_업로드만_한다() throws IOException {

        // given
        UUID bookId = UUID.randomUUID();

        Book existBook = createBookEntity(title, author, description, publisher, publishedDate,
            isbn, null, 0.0, 0L);
        BookUpdateRequest updateRequest = createUpdateRequest("new title", "new author",
            "new description", "new publisher", LocalDate.of(2023, 3, 3));

        MultipartFile newThumbnail = new MockMultipartFile(
            "coverImage",
            "newCover.png",
            "image/png",
            "dummy image data".getBytes()
        );

        Book updatedBook = createBookEntity("new title", "new author", "new description",
            "new publisher", LocalDate.of(2023, 3, 3), isbn, "newCover.com",
            0.0, 0L);

        String presignedUrl = "https://cdn.example.com/cover.png";

        BookDto expectedResponse = createBookDto(bookId, "new title", "new author", "new description",
            "new publisher", LocalDate.of(2023, 3, 3), isbn, presignedUrl, 0L, 0.0,
            Instant.now(), Instant.now());

        ReflectionTestUtils.setField(existBook, "id", bookId);
        ReflectionTestUtils.setField(updatedBook, "id", bookId);

        given(bookRepository.findById(bookId)).willReturn(Optional.of(existBook));
        given(storage.uploadImage(newThumbnail)).willReturn("newCover.com");
        given(bookRepository.save(any(Book.class))).willReturn(updatedBook);
        given(bookMapper.toDto(eq(updatedBook), any(S3Storage.class))).willReturn(expectedResponse);

        // when
        BookDto result = bookService.update(bookId, updateRequest, newThumbnail);

        // then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(storage).uploadImage(newThumbnail);
        verify(bookRepository).save(any(Book.class));
        verify(bookMapper).toDto(updatedBook, storage);
    }

    @Test
    void 썸네일이_있는_도서의_썸네일을_삭제하면_정상적으로_수정된다() throws IOException {

        UUID bookId = UUID.randomUUID();

        Book existBook = createBookEntity(title, author, description, publisher, publishedDate,
            isbn, "image/test.jpg", 0.0, 0L);
        BookUpdateRequest updateRequest = createUpdateRequest("new title", "new author",
            "new description", "new publisher", LocalDate.of(2023, 3, 3));

        MultipartFile emptyFile = new MockMultipartFile(
            "coverImage",
            "newCover.png",
            "image/png",
            new byte[0]
        );

        Book updatedBook = createBookEntity("new title", "new author", "new description",
            "new publisher", LocalDate.of(2023, 3, 3), isbn, null,
            0.0, 0L);

        BookDto expectedResponse = createBookDto(bookId, "new title", "new author", "new description",
            "new publisher", LocalDate.of(2023, 3, 3), isbn, null, 0L, 0.0,
            Instant.now(), Instant.now());

        ReflectionTestUtils.setField(existBook, "id", bookId);
        ReflectionTestUtils.setField(updatedBook, "id", bookId);

        given(bookRepository.findById(bookId)).willReturn(Optional.of(existBook));
        given(bookRepository.save(any(Book.class))).willReturn(updatedBook);
        given(bookMapper.toDto(eq(updatedBook), any(S3Storage.class))).willReturn(expectedResponse);

        // when
        BookDto result = bookService.update(bookId, updateRequest, emptyFile);

        // then
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(storage).deleteImage("image/test.jpg");
        verify(bookRepository).save(any(Book.class));
        verify(bookMapper).toDto(updatedBook, storage);
    }

    @Test
    void 존재하지_않는_도서를_수정_요청하면_수정에_실패한다() {

        // given
        UUID notExistBookId = UUID.randomUUID();
        BookUpdateRequest updateRequest = createUpdateRequest("new title", "new author",
            "new description", "new publisher", LocalDate.of(2022, 2, 2));

        given(bookRepository.findById(notExistBookId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> bookService.update(notExistBookId, updateRequest, null));

        // then
        assertThat(thrown)
            .isInstanceOf(BookNotFoundException.class);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void OCR_서비스_사용_가능_ISBN_추출_성공() throws OcrException {
        // given
        MultipartFile testImage = new MockMultipartFile("test", "test.jpg", "image/jpeg", "test content".getBytes());
        String expectedIsbn = "9780134685991";

        given(tesseractOcrExtractor.isAvailable()).willReturn(true);
        given(tesseractOcrExtractor.extractIsbn(any(MultipartFile.class))).willReturn(expectedIsbn);

        // when
        String result = bookService.extractIsbnFromImage(testImage);

        // then
        assertThat(result).isEqualTo(expectedIsbn);
        then(tesseractOcrExtractor).should().isAvailable();
        then(tesseractOcrExtractor).should().extractIsbn(testImage);
    }

    @Test
    void OCR_서비스_사용_불가능_예외_발생() {
        // given
        MultipartFile testImage = new MockMultipartFile("test", "test.jpg", "image/jpeg", "test content".getBytes());

        given(tesseractOcrExtractor.isAvailable()).willReturn(false);

        // when
        Throwable thrown = catchThrowable(() -> bookService.extractIsbnFromImage(testImage));

        // then
        assertThat(thrown)
            .isInstanceOf(OcrException.class)
            .hasMessageContaining("OCR 서버 내부 오류가 발생했습니다.");
        then(tesseractOcrExtractor).should().isAvailable();
        then(tesseractOcrExtractor).should(never()).extractIsbn(any());
    }

    @Test
    void OCR에서_null_반환_예외_발생() throws OcrException {
        // given
        MultipartFile testImage = new MockMultipartFile("test", "test.jpg", "image/jpeg", "test content".getBytes());

        given(tesseractOcrExtractor.isAvailable()).willReturn(true);
        given(tesseractOcrExtractor.extractIsbn(any(MultipartFile.class))).willReturn(null);

        // when
        Throwable thrown = catchThrowable(() -> bookService.extractIsbnFromImage(testImage));

        // then
        assertThat(thrown)
            .isInstanceOf(OcrException.class)
            .hasMessageContaining("OCR 서버 내부 오류가 발생했습니다.");
        then(tesseractOcrExtractor).should().isAvailable();
        then(tesseractOcrExtractor).should().extractIsbn(testImage);
    }

    @Test
    void OCR에서_빈_문자열_반환_예외_발생() throws OcrException {
        // given
        MultipartFile testImage = new MockMultipartFile("test", "test.jpg", "image/jpeg", "test content".getBytes());

        given(tesseractOcrExtractor.isAvailable()).willReturn(true);
        given(tesseractOcrExtractor.extractIsbn(any(MultipartFile.class))).willReturn("");

        // when
        Throwable thrown = catchThrowable(() -> bookService.extractIsbnFromImage(testImage));

        // then
        assertThat(thrown)
            .isInstanceOf(OcrException.class)
            .hasMessageContaining("OCR 서버 내부 오류가 발생했습니다.");
        then(tesseractOcrExtractor).should().isAvailable();
        then(tesseractOcrExtractor).should().extractIsbn(testImage);
    }

    @Test
    void OCR에서_공백만_있는_문자열_반환_예외_발생() throws OcrException {
        // given
        MultipartFile testImage = new MockMultipartFile("test", "test.jpg", "image/jpeg", "test content".getBytes());

        given(tesseractOcrExtractor.isAvailable()).willReturn(true);
        given(tesseractOcrExtractor.extractIsbn(any(MultipartFile.class))).willReturn("   ");

        // when
        Throwable thrown = catchThrowable(() -> bookService.extractIsbnFromImage(testImage));

        // then
        assertThat(thrown)
            .isInstanceOf(OcrException.class)
            .hasMessageContaining("OCR 서버 내부 오류가 발생했습니다.");
        then(tesseractOcrExtractor).should().isAvailable();
        then(tesseractOcrExtractor).should().extractIsbn(testImage);
    }

    @Test
    void OCR_처리_중_예외_발생() throws OcrException {
        // given
        MultipartFile testImage = new MockMultipartFile("test", "test.jpg", "image/jpeg", "test content".getBytes());

        given(tesseractOcrExtractor.isAvailable()).willReturn(true);
        given(tesseractOcrExtractor.extractIsbn(any(MultipartFile.class)))
            .willThrow(OcrException.serverError("OCR 처리 중 예상치 못한 오류가 발생했습니다."));

        // when
        Throwable thrown = catchThrowable(() -> bookService.extractIsbnFromImage(testImage));

        // then
        assertThat(thrown)
            .isInstanceOf(OcrException.class)
            .hasMessageContaining("OCR 서버 내부 오류가 발생했습니다.");
        then(tesseractOcrExtractor).should().isAvailable();
        then(tesseractOcrExtractor).should().extractIsbn(testImage);
    }

    @Test
    void OCR_처리_중_RuntimeException_발생() throws OcrException {
        // given
        MultipartFile testImage = new MockMultipartFile("test", "test.jpg", "image/jpeg", "test content".getBytes());

        given(tesseractOcrExtractor.isAvailable()).willReturn(true);
        given(tesseractOcrExtractor.extractIsbn(any(MultipartFile.class)))
            .willThrow(new RuntimeException("예상치 못한 오류"));

        // when
        Throwable thrown = catchThrowable(() -> bookService.extractIsbnFromImage(testImage));

        // then
        assertThat(thrown)
            .isInstanceOf(OcrException.class)
            .hasMessageContaining("OCR 서버 내부 오류가 발생했습니다.");
        then(tesseractOcrExtractor).should().isAvailable();
        then(tesseractOcrExtractor).should().extractIsbn(testImage);
    }

    @Test
    void 유효한_ISBN13_추출_성공() throws OcrException {
        // given
        MultipartFile testImage = new MockMultipartFile("test", "test.jpg", "image/jpeg", "test content".getBytes());
        String expectedIsbn = "9780134685991";

        given(tesseractOcrExtractor.isAvailable()).willReturn(true);
        given(tesseractOcrExtractor.extractIsbn(any(MultipartFile.class))).willReturn(expectedIsbn);

        // when
        String result = bookService.extractIsbnFromImage(testImage);

        // then
        assertThat(result).isEqualTo(expectedIsbn);
        assertThat(result).hasSize(13);
        assertThat(result).startsWith("978");
    }

    @Test
    void 유효한_ISBN10_추출_성공() throws OcrException {
        // given
        MultipartFile testImage = new MockMultipartFile("test", "test.jpg", "image/jpeg", "test content".getBytes());
        String expectedIsbn = "0134685997";

        given(tesseractOcrExtractor.isAvailable()).willReturn(true);
        given(tesseractOcrExtractor.extractIsbn(any(MultipartFile.class))).willReturn(expectedIsbn);

        // when
        String result = bookService.extractIsbnFromImage(testImage);

        // then
        assertThat(result).isEqualTo(expectedIsbn);
        assertThat(result).hasSize(10);
        assertThat(result).matches("\\d{10}");
    }

    @Test
    void 도서를_논리_삭제하면_isDeleted_필드가_true로_변경된다() {
        // given
        UUID bookId = UUID.randomUUID();
        Book book = createBookEntity(title, author, description, publisher, publishedDate, isbn, null, 0.0, 0L);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // when
        bookService.delete(bookId);

        // then
        assertTrue(book.isDeleted());
        verify(bookRepository).findById(bookId);
        verify(bookRepository, never()).delete(book); // 물리 삭제가 호출되지 않았는지도 검증
    }

    @Test
    void 도서_물리_삭제_성공() {
        // given
        UUID bookId = UUID.randomUUID();
        Book book = createBookEntity(
            title, author, description, publisher, publishedDate, isbn,
            "https://example.com/thumbnail.jpg", 4.5, 10L
        );
        book.delete(); // 소프트 삭제 상태로 설정

        given(bookRepository.findByIdIncludingDeleted(bookId)).willReturn(Optional.of(book));

        // when
        assertDoesNotThrow(() -> bookService.hardDelete(bookId));

        // then
        verify(bookRepository).findByIdIncludingDeleted(bookId);
        verify(bookRepository).hardDeleteBook(bookId);
    }

    @Test
    void 도서_물리_삭제_도서가_존재하지_않는_경우() {
        // given
        UUID bookId = UUID.randomUUID();
        given(bookRepository.findByIdIncludingDeleted(bookId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> bookService.hardDelete(bookId));

        // then
        assertThat(thrown).isInstanceOf(BookNotFoundException.class)
            .hasMessage("BOOK 찾을 수 없습니다");
        verify(bookRepository).findByIdIncludingDeleted(bookId);
        verify(bookRepository, never()).hardDeleteBook(any(UUID.class));
    }

    @Test
    void 도서_물리_삭제_소프트_삭제되지_않은_도서() {
        // given
        UUID bookId = UUID.randomUUID();
        Book book = createBookEntity(
            title, author, description, publisher, publishedDate, isbn,
            "https://example.com/thumbnail.jpg", 4.5, 10L
        );
        // isDeleted = false 상태 (소프트 삭제되지 않음)

        given(bookRepository.findByIdIncludingDeleted(bookId)).willReturn(Optional.of(book));

        // when
        Throwable thrown = catchThrowable(() -> bookService.hardDelete(bookId));

        // then
        assertThat(thrown).isInstanceOf(BookNotSoftDeletedException.class)
            .hasMessage("BOOK_NOT_SOFT_DELETED 잘못된 입력 값입니다.");
        verify(bookRepository).findByIdIncludingDeleted(bookId);
        verify(bookRepository, never()).hardDeleteBook(any(UUID.class));
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

    private BookUpdateRequest createUpdateRequest(String title, String author, String description,
        String publisher, LocalDate publishedDate) {

        return BookUpdateRequest.builder()
            .title(title)
            .author(author)
            .description(description)
            .publisher(publisher)
            .publishedDate(publishedDate)
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