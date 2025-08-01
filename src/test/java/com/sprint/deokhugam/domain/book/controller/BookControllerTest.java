package com.sprint.deokhugam.domain.book.controller;

import static com.sprint.deokhugam.fixture.BookFixture.createBookDto;
import static com.sprint.deokhugam.fixture.BookFixture.createRequest;
import static com.sprint.deokhugam.fixture.BookFixture.createUpdateRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.api.NaverBookInfoProvider;
import com.sprint.deokhugam.domain.api.dto.NaverBookDto;
import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sprint.deokhugam.domain.book.exception.BookNotFoundException;
import com.sprint.deokhugam.domain.book.exception.BookNotSoftDeletedException;
import com.sprint.deokhugam.domain.book.exception.InvalidFileTypeException;
import com.sprint.deokhugam.domain.book.exception.OcrException;
import com.sprint.deokhugam.domain.book.service.BookServiceImpl;
import com.sprint.deokhugam.domain.popularbook.dto.data.PopularBookDto;
import com.sprint.deokhugam.domain.popularbook.dto.request.PopularBookGetRequest;
import com.sprint.deokhugam.domain.popularbook.service.PopularBookServiceImpl;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(controllers = BookController.class)
@DisplayName("BookController 테스트")
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookServiceImpl bookService;

    @MockitoBean
    private NaverBookInfoProvider provider;

    @MockitoBean
    private PopularBookServiceImpl popularBookService;

    private CursorPageResponse<BookDto> mockResponse;
    private List<BookDto> mockBooks;
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

        BookDto book1 = createBookDto(UUID.randomUUID(), "테스트 도서 1", "테스트 저자 1",
            "테스트 설명 1", "테스트 출판사 1", LocalDate.of(2024, 1, 1),
            "1234567890", "https://example.com/image1.jpg", 10L, 4.5,
            Instant.now(), Instant.now());

        BookDto book2 = createBookDto(UUID.randomUUID(), "테스트 도서 2", "테스트 저자 2",
            "테스트 설명 2", "테스트 출판사 2", LocalDate.of(2024, 2, 1),
            "1234567891", "https://example.com/image2.jpg", 5L, 4.0,
            Instant.now(), Instant.now());

        mockBooks = List.of(book1, book2);
        mockResponse = new CursorPageResponse<>(
            mockBooks,
            null,
            null,
            mockBooks.size(),
            (long) mockBooks.size(),
            false
        );
    }

    @Test
    void 도서를_등록하면_201을_반환한다() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        String thumbnailUrl = "testUrl";

        BookCreateRequest request = createRequest(title, author, description, publisher,
            publishedDate, isbn);

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData", "", "application/json", objectMapper.writeValueAsBytes(request));

        MockMultipartFile thumbnailImage = new MockMultipartFile(
            "thumbnailImage", "thumbnail.png", "image/png", "fake-image-content".getBytes());

        BookDto expectedResponse = createBookDto(bookId, title, author, description, publisher,
            publishedDate, isbn, thumbnailUrl, 0L, 0.0, Instant.now(), Instant.now());

        given(bookService.create(any(BookCreateRequest.class), any(MultipartFile.class)))
            .willReturn(expectedResponse);

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books")
            .file(bookData)
            .file(thumbnailImage)
            .contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(bookId.toString()))
            .andExpect(jsonPath("$.title").value(title))
            .andExpect(jsonPath("$.author").value(author))
            .andExpect(jsonPath("$.description").value(description))
            .andExpect(jsonPath("$.publisher").value(publisher))
            .andExpect(jsonPath("$.publishedDate").value(publishedDate.toString()))
            .andExpect(jsonPath("$.isbn").value(isbn));
        verify(bookService).create(argThat(req ->
            req.title().equals(title) &&
                req.author().equals(author) &&
                req.description().equals(description) &&
                req.publisher().equals(publisher) &&
                req.publishedDate().equals(publishedDate) &&
                req.isbn().equals(isbn)
        ), any());
    }

    @Test
    void 출간일을_현재_날짜_보다_이후로_설정하면_400_에러를_반환한다() throws Exception {

        // given
        BookCreateRequest request = createRequest(title, author, description, publisher,
            LocalDate.of(2099, 7, 12), isbn);

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData",
            "",
            "application/json",
            objectMapper.writeValueAsBytes(request));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books")
            .file(bookData)
            .contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 필수_항목이_빈_값이면_400_에러를_반환한다() throws Exception {
        // given
        // 제목을 입력하지 않은 경우
        BookCreateRequest request = createRequest(null, author, description, publisher,
            publishedDate, isbn);

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData",
            "",
            "application/json",
            objectMapper.writeValueAsBytes(request));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books")
            .file(bookData)
            .contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 제목의_길이가_100자_초과이면_400_에러를_반환한다() throws Exception {

        // given
        String longTitle = "a".repeat(101);

        BookCreateRequest request = createRequest(longTitle, author, description, publisher,
            publishedDate, isbn);

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData",
            "",
            "application/json",
            objectMapper.writeValueAsBytes(request));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books")
            .file(bookData)
            .contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void isbn_길이가_13자_초과하면_400_에러를_반환한다() throws Exception {

        // given
        BookCreateRequest request = createRequest(title, author, description, publisher,
            publishedDate, "12345678901234");

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData",
            "",
            "application/json",
            objectMapper.writeValueAsBytes(request));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books")
            .file(bookData)
            .contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 도서_목록을_기본값으로_조회한다() throws Exception {
        // given
        given(bookService.getBooks(any(BookSearchRequest.class))).willReturn(mockResponse);

        // when
        ResultActions result = mockMvc.perform(get("/api/books")
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].title").value("테스트 도서 1"))
            .andExpect(jsonPath("$.content[1].title").value("테스트 도서 2"))
            .andExpect(jsonPath("$.hasNext").value(false))
            .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void 도서_목록을_키워드로_조회한다() throws Exception {
        // given
        given(bookService.getBooks(any(BookSearchRequest.class))).willReturn(mockResponse);

        // when
        ResultActions result = mockMvc.perform(get("/api/books")
            .param("keyword", "테스트")
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].title").value("테스트 도서 1"))
            .andExpect(jsonPath("$.content[1].title").value("테스트 도서 2"))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 도서_목록을_정렬해서_조회한다() throws Exception {
        // given
        given(bookService.getBooks(any(BookSearchRequest.class))).willReturn(mockResponse);

        // when
        ResultActions result = mockMvc.perform(get("/api/books")
            .param("orderBy", "rating")
            .param("direction", "DESC")
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].rating").value(4.5))
            .andExpect(jsonPath("$.content[1].rating").value(4.0));
    }

    @Test
    void 도서_목록을_페이지_크기를_정해서_조회한다() throws Exception {
        // given
        given(bookService.getBooks(any(BookSearchRequest.class))).willReturn(mockResponse);

        // when
        ResultActions result = mockMvc.perform(get("/api/books")
            .param("limit", "1")
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2)); // Mock 데이터가 2개로 설정되어 있음
    }

    @Test
    void 도서_목록_조회시_빈_결과를_반환한다() throws Exception {
        // given
        CursorPageResponse<BookDto> emptyResponse = new CursorPageResponse<>(
            List.of(),
            null,
            null,
            0,
            0L,
            false
        );
        given(bookService.getBooks(any(BookSearchRequest.class))).willReturn(emptyResponse);

        // when
        ResultActions result = mockMvc.perform(get("/api/books")
            .param("keyword", "존재하지 않는 책")
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 도서_상세_조회_요청_시_도서_상세_정보를_반환한다() throws Exception {

        // given
        UUID bookId = UUID.randomUUID();
        BookDto book = createBookDto(bookId, title, author, description, publisher, publishedDate,
            isbn, "https://example.com/image1.jpg", 10L, 4.5,
            Instant.now(), Instant.now());

        given(bookService.findById(bookId)).willReturn(book);

        // when
        ResultActions result = mockMvc.perform(get("/api/books/" + bookId));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(bookId.toString()))
            .andExpect(jsonPath("$.title").value(title))
            .andExpect(jsonPath("$.author").value(author))
            .andExpect(jsonPath("$.description").value(description))
            .andExpect(jsonPath("$.publisher").value(publisher))
            .andExpect(jsonPath("$.publishedDate").value(publishedDate.toString()))
            .andExpect(jsonPath("$.isbn").value(isbn))
            .andExpect(jsonPath("$.thumbnailUrl").value("https://example.com/image1.jpg"))
            .andExpect(jsonPath("$.rating").value(4.5))
            .andExpect(jsonPath("$.reviewCount").value(10L));
    }

    @Test
    void OCR_API_호출_성공_ISBN_추출_성공() throws Exception {
        // given
        String expectedIsbn = "9780134685991";
        MockMultipartFile testImage = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        given(bookService.extractIsbnFromImage(any(MultipartFile.class))).willReturn(expectedIsbn);

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr")
            .file(testImage));

        // then
        result.andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(expectedIsbn));
        then(bookService).should().extractIsbnFromImage(any(MultipartFile.class));
    }

    @Test
    void OCR_API_호출_이미지_파일_없음() throws Exception {
        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr"));

        // then
        result.andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void OCR_API_호출_빈_이미지_파일() throws Exception {
        // given
        MockMultipartFile emptyImage = new MockMultipartFile(
            "image",
            "empty.jpg",
            "image/jpeg",
            new byte[0]
        );

        given(bookService.extractIsbnFromImage(any(MultipartFile.class)))
            .willThrow(new IllegalArgumentException("빈 이미지 파일은 처리할 수 없습니다."));

        // when
        ResultActions result = mockMvc.perform(
            multipart("/api/books/isbn/ocr")
                .file(emptyImage)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // then
        result.andExpect(status().isBadRequest());

        verify(bookService).extractIsbnFromImage(any(MultipartFile.class));
    }


    @Test
    void OCR_API_호출_지원하지_않는_파일_형식() throws Exception {
        // given
        MockMultipartFile emptyImage = new MockMultipartFile(
            "image", "empty.jpg", "image/jpeg", new byte[0]);

        when(bookService.extractIsbnFromImage(any(MultipartFile.class)))
            .thenThrow(new IllegalArgumentException("빈 이미지 파일은 처리할 수 없습니다."));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr")
            .file(emptyImage));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void OCR_API_호출_파일_크기_제한_초과() throws Exception {
        // given
        byte[] largeImageData = new byte[6 * 1024 * 1024]; // 6MB 데이터
        MockMultipartFile largeImage = new MockMultipartFile(
            "image",
            "large.jpg",
            "image/jpeg",
            largeImageData
        );

        given(bookService.extractIsbnFromImage(any(MultipartFile.class)))
            .willThrow(new IllegalArgumentException("파일 크기가 5MB를 초과합니다."));

        // when
        ResultActions result = mockMvc.perform(
            multipart("/api/books/isbn/ocr")
                .file(largeImage)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // then
        result.andExpect(status().isBadRequest());

        verify(bookService).extractIsbnFromImage(any(MultipartFile.class));
    }

    @Test
    void OCR_API_호출_지원하지_않는_이미지_형식() throws Exception {
        // given
        MockMultipartFile unsupportedImage = new MockMultipartFile(
            "image",
            "test.tiff",
            "image/tiff", // 지원하지 않는 형식
            "test content".getBytes()
        );

        // Mock 서비스가 InvalidFileTypeException을 던지도록 설정
        given(bookService.extractIsbnFromImage(any(MultipartFile.class)))
            .willThrow(new InvalidFileTypeException("지원하지 않는 파일 형식입니다. (지원 형식: jpg, jpeg, png)"));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr")
            .file(unsupportedImage));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FILE_INVALID_INPUT_VALUE"))
            .andExpect(jsonPath("$.message").value("FILE 잘못된 입력 값입니다."))
            .andExpect(jsonPath("$.details.contentType").value(
                "지원하지 않는 파일 형식입니다. (지원 형식: jpg, jpeg, png)"));
    }

    @Test
    void OCR_API_호출_서비스에서_OcrException_발생() throws Exception {
        // given
        MockMultipartFile testImage = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test content".getBytes()
        );

        given(bookService.extractIsbnFromImage(any(MultipartFile.class)))
            .willThrow(OcrException.serverError("OCR 서버 내부 오류가 발생했습니다."));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr")
            .file(testImage));

        // then
        result.andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("OCR_INTERNAL_SERVER_ERROR"))
            .andExpect(jsonPath("$.message").value("OCR 서버 내부 오류가 발생했습니다."));
    }

    @Test
    void OCR_API_호출_서비스에서_RuntimeException_발생() throws Exception {
        // given
        MockMultipartFile testImage = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test content".getBytes()
        );

        given(bookService.extractIsbnFromImage(any(MultipartFile.class)))
            .willThrow(new RuntimeException("예상치 못한 오류"));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr")
            .file(testImage));

        // then
        result.andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void OCR_API_호출_다양한_유효한_이미지_형식() throws Exception {
        // given
        String expectedIsbn = "9780134685991";
        String[] validFormats = {"image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"};
        String[] fileExtensions = {"jpg", "png", "gif", "bmp", "webp"};

        given(bookService.extractIsbnFromImage(any(MultipartFile.class))).willReturn(expectedIsbn);

        for (int i = 0; i < validFormats.length; i++) {
            // given
            MockMultipartFile testImage = new MockMultipartFile(
                "image",
                "test." + fileExtensions[i],
                validFormats[i],
                "test content".getBytes()
            );

            // when
            ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr")
                .file(testImage));

            // then
            result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(expectedIsbn));
        }
    }

    @Test
    void OCR_API_호출_null_ContentType_처리() throws Exception {
        // given
        MockMultipartFile testImage = new MockMultipartFile(
            "image",
            "test.jpg",
            null,
            "test content".getBytes()
        );

        // Mock 서비스가 InvalidFileTypeException을 던지도록 설정
        given(bookService.extractIsbnFromImage(any(MultipartFile.class)))
            .willThrow(new InvalidFileTypeException("[BookService] 이미지 파일만 업로드 가능합니다."));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr")
            .file(testImage));

        // then
        result.andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("FILE_INVALID_INPUT_VALUE"))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void isbn으로_도서_정보_요청_시_도서_정보를_반환한다() throws Exception {

        // given
        NaverBookDto bookDto = NaverBookDto.builder()
            .title(title)
            .author(author)
            .description(description)
            .publisher(publisher)
            .publishedDate(publishedDate)
            .isbn(isbn)
            .thumbnailImage("/imageExample")
            .build();

        given(provider.fetchInfoByIsbn(isbn)).willReturn(bookDto);

        // when
        ResultActions result = mockMvc.perform(get("/api/books/info?isbn=" + isbn));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value(title))
            .andExpect(jsonPath("$.author").value(author))
            .andExpect(jsonPath("$.description").value(description))
            .andExpect(jsonPath("$.publisher").value(publisher))
            .andExpect(jsonPath("$.publishedDate").value(publishedDate.toString()))
            .andExpect(jsonPath("$.isbn").value(isbn))
            .andExpect(jsonPath("$.thumbnailImage").value("/imageExample"));
    }

    @Test
    void 도서를_수정하면_200을_반환한다() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        String thumbnailUrl = "testUrl";

        BookUpdateRequest updateRequest = createUpdateRequest(title, author, description, publisher,
            publishedDate);

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData", "", "application/json", objectMapper.writeValueAsBytes(updateRequest));

        MockMultipartFile thumbnailImage = new MockMultipartFile(
            "thumbnailImage", "thumbnail.png", "image/png", "fake-image-content".getBytes());

        BookDto expectedResponse = createBookDto(bookId, title, author, description, publisher,
            publishedDate, isbn, thumbnailUrl, 0L, 0.0,
            Instant.now(), Instant.now());

        given(bookService.update(any(UUID.class), any(BookUpdateRequest.class),
            any(MultipartFile.class)))
            .willReturn(expectedResponse);

        // when
        ResultActions result = mockMvc.perform(
            multipart("/api/books/" + bookId)
                .file(bookData)
                .file(thumbnailImage)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                })
        );

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(bookId.toString()))
            .andExpect(jsonPath("$.title").value(title))
            .andExpect(jsonPath("$.author").value(author))
            .andExpect(jsonPath("$.description").value(description))
            .andExpect(jsonPath("$.publisher").value(publisher))
            .andExpect(jsonPath("$.publishedDate").value(publishedDate.toString()))
            .andExpect(jsonPath("$.isbn").value(isbn))
            .andExpect(jsonPath("$.thumbnailUrl").value(thumbnailUrl));
    }

    @Test
    void 도서_정보_수정_요청_시_저자가_50자_초과이면_400_에러를_반환한다() throws Exception {

        // given
        UUID bookId = UUID.randomUUID();
        String longAuthor = "a".repeat(51);

        BookUpdateRequest updateRequest = createUpdateRequest(title, longAuthor, description,
            publisher,
            publishedDate);

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData",
            "",
            "application/json",
            objectMapper.writeValueAsBytes(updateRequest));

        // when
        ResultActions result = mockMvc.perform(
            multipart("/api/books/" + bookId)
                .file(bookData)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                })
        );

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 존재하는_도서_ID로_삭제_요청하면_204_상태코드를_반환한다() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        doNothing().when(bookService).delete(bookId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/books/{bookId}", bookId));

        // then
        result.andExpect(status().isNoContent());
        verify(bookService).delete(bookId);
    }

    @Test
    void 존재하지_않는_도서_ID로_삭제_요청하면_404_상태코드를_반환한다() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        doThrow(new BookNotFoundException(bookId)).when(bookService).delete(bookId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/books/{bookId}", bookId));

        // then
        result.andExpect(status().isNotFound());
        verify(bookService).delete(bookId);
    }

    @Test
    void 도서_물리_삭제_API_성공() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        doNothing().when(bookService).hardDelete(bookId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/books/{bookId}/hard", bookId));

        // then
        result.andExpect(status().isNoContent());
        verify(bookService).hardDelete(bookId);
    }

    @Test
    void 도서_물리_삭제_API_도서가_존재하지_않는_경우() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        doThrow(new BookNotFoundException(bookId))
            .when(bookService).hardDelete(bookId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/books/{bookId}/hard", bookId));

        // then
        result.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("BOOK 찾을 수 없습니다"));
        verify(bookService).hardDelete(bookId);
    }

    @Test
    void 도서_물리_삭제_API_소프트_삭제되지_않은_도서() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        doThrow(new BookNotSoftDeletedException(bookId))
            .when(bookService).hardDelete(bookId);

        // when
        ResultActions result = mockMvc.perform(delete("/api/books/{bookId}/hard", bookId));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BOOK_NOT_SOFT_DELETED_INVALID_INPUT_VALUE"))
            .andExpect(jsonPath("$.message").value("BOOK_NOT_SOFT_DELETED 잘못된 입력 값입니다."));
        verify(bookService).hardDelete(bookId);
    }

    @Test
    void 도서_물리_삭제_API_잘못된_UUID_형식() throws Exception {
        // given
        String invalidBookId = "invalid-uuid-format";

        // when
        ResultActions result = mockMvc.perform(delete("/api/books/{bookId}/hard", invalidBookId));

        // then
        result.andExpect(status().isBadRequest());
        verify(bookService, never()).hardDelete(any(UUID.class));
    }

    @Test
    void 인기_도서_목록_조회를_하면_200을_반환한다() throws Exception {

        // given
        UUID book1Id = UUID.randomUUID();
        UUID book2Id = UUID.randomUUID();
        UUID book3Id = UUID.randomUUID();
        UUID pb1Id = UUID.randomUUID();
        UUID pb2Id = UUID.randomUUID();
        UUID pb3Id = UUID.randomUUID();

        PopularBookDto dto1 = new PopularBookDto(pb1Id, book1Id, "test book1", "test author1",
            "test.jpg", PeriodType.DAILY, 1L,
            4.4, 5L, 4.0, Instant.now());
        PopularBookDto dto2 = new PopularBookDto(pb2Id, book2Id, "test book2", "test author2", null,
            PeriodType.DAILY, 3L, 3.6, 2L, 4.0, Instant.now());
        PopularBookDto dto3 = new PopularBookDto(pb3Id, book3Id, "test book3", "test author3", null,
            PeriodType.DAILY, 2L, 4.0, 4L, 4.0, Instant.now());

        PopularBookGetRequest request = PopularBookGetRequest.builder()
            .period(PeriodType.DAILY)
            .direction("ASC")
            .limit(3)
            .build();

        CursorPageResponse<PopularBookDto> response = new CursorPageResponse<>(
            List.of(dto1, dto3, dto2),
            null, null, 3, 3L, false);

        given(popularBookService.getPopularBooks(request)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/books/popular")
            .param("period", "DAILY")
            .param("direction", "ASC")
            .param("limit", "3"));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.content[1].title").value("test book3"))
            .andExpect(jsonPath("$.content[2].title").value("test book2"))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 잘못된_랭킹_기간으로_인기_도서_목록_조회를_하면_400을_반환한다() throws Exception {

        // given
        String invalidPeriod = "YEARLY";

        // when
        ResultActions result = mockMvc.perform(get("/api/books/popular?period=" + invalidPeriod));

        // then
        result.andExpect(status().isBadRequest());
    }
}