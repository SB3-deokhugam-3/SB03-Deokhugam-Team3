package com.sprint.deokhugam.integration;

import static com.sprint.deokhugam.fixture.BookFixture.createBookEntity;
import static com.sprint.deokhugam.fixture.BookFixture.createRequest;
import static com.sprint.deokhugam.fixture.BookFixture.createUpdateRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.dto.request.BookUpdateRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.ocr.TesseractOcrExtractor;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.book.storage.s3.S3Storage;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private S3Storage storage; // 실제 업로드 방지

    @MockitoBean
    private TesseractOcrExtractor tesseractOcrExtractor;

    @Test
    void 책_등록_성공_통합_테스트() throws Exception {

        // given
        BookCreateRequest request = createRequest("test book", "test author", "test description",
            "test publisher", LocalDate.of(1999, 7, 2), "1999070212345");

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData", "", "application/json",
            objectMapper.writeValueAsBytes(request));

        MockMultipartFile thumbnail = new MockMultipartFile(
            "thumbnailImage",
            "cover.png",
            "image/png",
            "dummy image data".getBytes()
        );

        given(storage.uploadImage(any(MultipartFile.class))).willReturn(
            "https://mock-s3.com/cover.png");

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books")
            .file(bookData)
            .file(thumbnail)
            .contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        result.andExpect(status().isCreated());
        result.andExpect(jsonPath("$.title").value("test book"));
        result.andExpect(jsonPath("$.isbn").value("1999070212345"));
        // 저장 확인 (DB)
        List<Book> books = bookRepository.findAll();
        assertEquals(1, books.size());
        assertEquals("test book", books.get(0).getTitle());
    }

    @Test
    void 중복된_isbn으로_책_등록_요청_시_409_에러를_반환한다() throws Exception {

        // given
        Book book = createBookEntity("테스트 코드 작성하기 싫다", "한동우", "정말 하기 싫다",
            "하기 싫엉", LocalDate.of(1999, 7, 2), "1999070212345",
            null, 0.0, 0L);
        bookRepository.save(book);

        BookCreateRequest request = createRequest("test book", "test author", "test description",
            "test publisher", LocalDate.of(1999, 7, 2), "1999070212345");

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData", "", "application/json",
            objectMapper.writeValueAsBytes(request));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books")
            .file(bookData)
            .contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        result.andExpect(status().isConflict());
    }

    @Test
    void 잘못된_형식으로_책_등록_요청_시_400_에러를_반환한다() throws Exception {

        // given
        // 잘못된 isbn 형식
        BookCreateRequest request = createRequest("test book", "test author", "test description",
            "test publisher", LocalDate.of(1999, 7, 2), "199907021234567");

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData", "", "application/json",
            objectMapper.writeValueAsBytes(request));

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books")
            .file(bookData)
            .contentType(MediaType.MULTIPART_FORM_DATA));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 책_수정_통합_테스트() throws Exception {

        // given
        Book book = createBookEntity("test book", "test author", "test description",
            "test publisher", LocalDate.of(1999, 7, 2), "1999070212345",
            null, 0.0, 0L);
        Book savedBook = bookRepository.save(book);
        UUID bookId = savedBook.getId();

        BookUpdateRequest updateRequest = createUpdateRequest("책책책", "test author", "test description",
            "test publisher", LocalDate.of(1999, 7, 2));

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData", "", "application/json",
            objectMapper.writeValueAsBytes(updateRequest));

        MockMultipartFile thumbnail = new MockMultipartFile(
            "thumbnailImage",
            "cover.png",
            "image/png",
            "dummy image data".getBytes()
        );

        given(storage.uploadImage(any(MultipartFile.class))).willReturn(
            "https://mock-s3.com/cover.png");

        // when
        ResultActions result = mockMvc.perform(
            multipart("/api/books/" + bookId)
                .file(bookData)
                .file(thumbnail)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                })
        );

        // then
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.title").value("책책책"));
        // DB 적용 확인
        Optional<Book> foundBook = bookRepository.findById(bookId);
        assertTrue(foundBook.isPresent());
        assertEquals("책책책", foundBook.get().getTitle());
        assertEquals("https://mock-s3.com/cover.png", foundBook.get().getThumbnailUrl());
    }

    @Test
    void 도서_상세_조회_통합_테스트() throws Exception {
        // given
        Book book = createBookEntity("조회 테스트 도서", "저자", "설명",
            "출판사", LocalDate.of(2024, 1, 1), "1234567890123",
            "https://example.com/cover.jpg", 4.5, 10L);
        Book savedBook = bookRepository.save(book);
        UUID bookId = savedBook.getId();

        // when
        ResultActions result = mockMvc.perform(get("/api/books/" + bookId));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("조회 테스트 도서"))
            .andExpect(jsonPath("$.author").value("저자"))
            .andExpect(jsonPath("$.rating").value(4.5))
            .andExpect(jsonPath("$.reviewCount").value(10));
    }

    @Test
    void 존재하지_않는_도서_조회_시_404_에러() throws Exception {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(get("/api/books/" + nonExistentId));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    void 도서_목록_조회_통합_테스트() throws Exception {
        // given
        Book book1 = createBookEntity("자바 프로그래밍", "김자바", "자바 학습서",
            "IT출판사", LocalDate.of(2024, 1, 1), "1111111111111", null, 4.0, 5L);
        Book book2 = createBookEntity("스프링 부트", "박스프링", "스프링 학습서",
            "IT출판사", LocalDate.of(2024, 2, 1), "2222222222222", null, 4.5, 8L);
        bookRepository.saveAll(List.of(book1, book2));

        // when
        ResultActions result = mockMvc.perform(get("/api/books")
            .param("orderBy", "title")
            .param("direction", "ASC")
            .param("limit", "10"));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].title").value("스프링 부트"))
            .andExpect(jsonPath("$.content[1].title").value("자바 프로그래밍"));
    }

    @Test
    void 키워드_검색_통합_테스트() throws Exception {
        // given
        Book book1 = createBookEntity("자바 프로그래밍", "김자바", "자바 학습서",
            "IT출판사", LocalDate.of(2024, 1, 1), "1111111111111", null, 4.0, 5L);
        Book book2 = createBookEntity("파이썬 기초", "이파이썬", "파이썬 학습서",
            "IT출판사", LocalDate.of(2024, 2, 1), "2222222222222", null, 4.5, 8L);
        bookRepository.saveAll(List.of(book1, book2));

        // when
        ResultActions result = mockMvc.perform(get("/api/books")
            .param("keyword", "자바")
            .param("limit", "10"));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("자바 프로그래밍"));
    }

    @Test
    void 도서_논리_삭제_통합_테스트() throws Exception {
        // given
        Book book = createBookEntity("삭제될 도서", "저자", "설명",
            "출판사", LocalDate.of(2024, 1, 1), "1234567890123", null, 4.0, 5L);
        Book savedBook = bookRepository.save(book);
        UUID bookId = savedBook.getId();

        // when
        ResultActions deleteResult = mockMvc.perform(delete("/api/books/" + bookId));

        // then
        deleteResult.andExpect(status().isNoContent());
        bookRepository.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/books/" + bookId))
            .andExpect(status().isNotFound());
        Optional<Book> deletedBook = bookRepository.findByIdIncludingDeleted(bookId);
        assertTrue(deletedBook.isPresent());
        assertTrue(deletedBook.get().isDeleted());
    }

    @Test
    void 도서_물리_삭제_통합_테스트() throws Exception {
        // given
        Book book = createBookEntity("물리삭제될 도서", "저자", "설명",
            "출판사", LocalDate.of(2024, 1, 1), "1234567890123", null, 4.0, 5L);
        book.delete(); // 먼저 논리 삭제
        Book savedBook = bookRepository.save(book);
        UUID bookId = savedBook.getId();

        // when
        ResultActions result = mockMvc.perform(delete("/api/books/" + bookId + "/hard"));

        // then
        result.andExpect(status().isNoContent());
        Optional<Book> deletedBook = bookRepository.findByIdIncludingDeleted(bookId);
        assertFalse(deletedBook.isPresent());
    }

}
