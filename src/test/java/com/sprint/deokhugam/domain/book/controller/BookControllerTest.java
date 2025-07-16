package com.sprint.deokhugam.domain.book.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.exception.OcrException;
import com.sprint.deokhugam.domain.book.service.BookServiceImpl;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(controllers = BookController.class)
@DisplayName("BookController 테스트")
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookServiceImpl bookService;

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
            "1234567890", "https://example.com/image1.jpg", 10L, 4.5);

        BookDto book2 = createBookDto(UUID.randomUUID(), "테스트 도서 2", "테스트 저자 2",
            "테스트 설명 2", "테스트 출판사 2", LocalDate.of(2024, 2, 1),
            "1234567891", "https://example.com/image2.jpg", 5L, 4.0);

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

        BookCreateRequest request = createRequest(title, author, description, publisher, publishedDate, isbn);

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData", "", "application/json", objectMapper.writeValueAsBytes(request));

        MockMultipartFile thumbnailImage = new MockMultipartFile(
            "thumbnailImage", "thumbnail.png", "image/png", "fake-image-content".getBytes());

        BookDto expectedResponse = createBookDto(bookId, title, author, description, publisher,
            publishedDate, isbn, thumbnailUrl, 0L, 0.0);

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
    @DisplayName("도서 목록 조회 - 기본 파라미터")
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
    @DisplayName("도서 목록 조회 - 키워드 검색")
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
    @DisplayName("도서 목록 조회 - 정렬 조건 설정")
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
    @DisplayName("도서 목록 조회 - 페이지 크기 설정")
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
    @DisplayName("도서 목록 조회 - 빈 결과")
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
            isbn, "https://example.com/image1.jpg", 10L, 4.5);

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
    @DisplayName("OCR API 호출 성공 - ISBN 추출 성공")
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
    @DisplayName("OCR API 호출 - 이미지 파일 없음")
    void OCR_API_호출_이미지_파일_없음() throws Exception {
        // given
        // 이미지 파일 없이 요청

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr"));

        // then
        result.andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("OCR API 호출 - 빈 이미지 파일")
    void OCR_API_호출_빈_이미지_파일() throws Exception {
        // given
        MockMultipartFile emptyImage = new MockMultipartFile(
            "image",
            "empty.jpg",
            "image/jpeg",
            new byte[0]
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr")
            .file(emptyImage));

        // then
        result.andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("OCR API 호출 - 지원하지 않는 파일 형식")
    void OCR_API_호출_지원하지_않는_파일_형식() throws Exception {
        // given
        MockMultipartFile textFile = new MockMultipartFile(
            "image",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr")
            .file(textFile));

        // then
        result.andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("OCR API 호출 - 파일 크기 제한 초과")
    void OCR_API_호출_파일_크기_제한_초과() throws Exception {
        // given
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeImage = new MockMultipartFile(
            "image",
            "large.jpg",
            "image/jpeg",
            largeContent
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr")
            .file(largeImage));

        // then
        result.andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("OCR API 호출 - 지원하지 않는 이미지 형식")
    void OCR_API_호출_지원하지_않는_이미지_형식() throws Exception {
        // given
        MockMultipartFile unsupportedImage = new MockMultipartFile(
            "image",
            "test.tiff",
            "image/tiff",
            "test content".getBytes()
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr")
            .file(unsupportedImage));

        // then
        result.andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("OCR API 호출 - 서비스에서 OcrException 발생")
    void OCR_API_호출_서비스에서_OcrException_발생() throws Exception {
        // given
        MockMultipartFile testImage = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test content".getBytes()
        );

        given(bookService.extractIsbnFromImage(any(MultipartFile.class)))
            .willThrow(new OcrException("OCR 처리 오류"));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr")
            .file(testImage));

        // then
        result.andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("OCR API 호출 - 서비스에서 RuntimeException 발생")
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
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("OCR API 호출 - 다양한 유효한 이미지 형식")
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
    @DisplayName("OCR API 호출 - null ContentType 처리")
    void OCR_API_호출_null_ContentType_처리() throws Exception {
        // given
        MockMultipartFile testImage = new MockMultipartFile(
            "image",
            "test.jpg",
            null, // null contentType
            "test content".getBytes()
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books/isbn/ocr")
            .file(testImage));

        // then
        result.andDo(print())
            .andExpect(status().isInternalServerError());
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
        Double rating) {
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
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }
}