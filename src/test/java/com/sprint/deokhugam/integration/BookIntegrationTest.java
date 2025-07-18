package com.sprint.deokhugam.integration;

import static com.sprint.deokhugam.fixture.BookFixture.createBookEntity;
import static com.sprint.deokhugam.fixture.BookFixture.createRequest;
import static com.sprint.deokhugam.fixture.BookFixture.createUpdateRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class BookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
}
