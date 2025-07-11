package com.sprint.deokhugam.domain.book.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.service.BookServiceImpl;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(BookController.class)
@DisplayName("BookController 슬라이스 테스트")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookServiceImpl bookService;

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
}