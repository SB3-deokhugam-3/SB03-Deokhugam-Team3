package com.sprint.deokhugam.domain.book.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.deokhugam.domain.book.service.BookServiceImpl;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(BookController.class)
@DisplayName("BookController 슬라이스 테스트")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookServiceImpl bookService;

    @Test
    void 도서를_등록하면_201을_반환한다() throws Exception {

        // given
        String bookDataJson = """
              {
                "title": "test book",
                "author": "test author",
                "description": "test description",
                "publisher": "test publisher",
                "publishedDate": "2025-07-11",
                "isbn": "1234567890123"
              }
            """;

        MockMultipartFile bookData = new MockMultipartFile(
            "bookData", "", "application/json", bookDataJson.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile thumbnailImage = new MockMultipartFile(
            "thumbnailImage", "thumbnail.png", "image/png", "fake-image-content".getBytes());

        // when
        ResultActions result = mockMvc.perform(multipart("/api/books")
            .file(bookData)
            .file(thumbnailImage));

        // then
        result.andExpect(status().isCreated());
    }
}