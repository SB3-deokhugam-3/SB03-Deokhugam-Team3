package com.sprint.deokhugam.domain.book.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
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
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookServiceImpl bookService;
    private CursorPageResponse<BookDto> mockResponse;
    private List<BookDto> mockBooks;

    @BeforeEach
    void 초기_설정() {
        BookDto book1 = new BookDto(
            UUID.randomUUID(),
            "테스트 도서 1",
            "테스트 저자 1",
            "테스트 설명 1",
            "테스트 출판사 1",
            LocalDate.of(2024, 1, 1),
            "1234567890",
            "https://example.com/image1.jpg",
            10L,
            4.5,
            Instant.now(),
            Instant.now()
        );

        BookDto book2 = new BookDto(
            UUID.randomUUID(),
            "테스트 도서 2",
            "테스트 저자 2",
            "테스트 설명 2",
            "테스트 출판사 2",
            LocalDate.of(2024, 2, 1),
            "1234567891",
            "https://example.com/image2.jpg",
            5L,
            4.0,
            Instant.now(),
            Instant.now()
        );

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

        String title = "test book";
        String author = "test author";
        String description = "test description";
        String publisher = "test publisher";
        LocalDate publishedDate = LocalDate.now();
        String isbn = "1234567890123";

        BookCreateRequest request = BookCreateRequest.builder()
            .title(title)
            .author(author)
            .description(description)
            .publisher(publisher)
            .publishedDate(publishedDate)
            .isbn(isbn)
            .build();

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData", "", "application/json", objectMapper.writeValueAsBytes(request));

        MockMultipartFile thumbnailImage = new MockMultipartFile(
            "thumbnailImage", "thumbnail.png", "image/png", "fake-image-content".getBytes());

        BookDto expectedResponse = BookDto.builder()
            .id(bookId)
            .title(title)
            .author(author)
            .description(description)
            .publisher(publisher)
            .publishedDate(publishedDate)
            .isbn(isbn)
            .thumbnailUrl(thumbnailUrl)
            .reviewCount(0L)
            .rating(0.0)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

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
        BookCreateRequest request = BookCreateRequest.builder()
            .title("test book")
            .author("test author")
            .description("test description")
            .publisher("test publisher")
            .publishedDate(LocalDate.of(2099, 7, 12))
            .isbn("1234567890123")
            .build();

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
        BookCreateRequest request = BookCreateRequest.builder()
            .author("test author")
            .description("test description")
            .publisher("test publisher")
            .publishedDate(LocalDate.now())
            .isbn("1234567890123123")
            .build();

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
    void 제목의_길이가_100자_이상이면_400_에러를_반환한다() throws Exception {

        // given
        String longTitle = "a".repeat(101);

        BookCreateRequest request = BookCreateRequest.builder()
            .title(longTitle)
            .author("test author")
            .description("test description")
            .publisher("test publisher")
            .publishedDate(LocalDate.now())
            .isbn("1234567890123")
            .build();

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
    void isbn_길이가_13자_이상이면_400_에러를_반환한다() throws Exception {

        // given
        BookCreateRequest request = BookCreateRequest.builder()
            .title("test book")
            .author("test author")
            .description("test description")
            .publisher("test publisher")
            .publishedDate(LocalDate.now())
            .isbn("1234567890123123")
            .build();

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
}