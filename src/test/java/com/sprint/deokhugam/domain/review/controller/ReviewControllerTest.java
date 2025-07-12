package com.sprint.deokhugam.domain.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.service.ReviewService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ReviewController.class)
@DisplayName("ReviewController 슬라이스 테스트")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 리뷰_생성_성공() throws Exception {
        // given
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        Instant now = Instant.now();
        ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "굿 입니당~", 4.2);
        ReviewDto dto = ReviewDto.builder()
            .id(reviewId)
            .bookId(bookId)
            .bookTitle("테스트 책")
            .bookThumbnailUrl("http://image.url")
            .userId(userId)
            .userNickname("테스터")
            .content("굿 입니당~")
            .rating(4.2)
            .likeCount(0L)
            .commentCount(0L)
            .likedByMe(false)
            .createdAt(now)
            .updatedAt(now)
            .build();
        given(reviewService.create(any())).willReturn(dto);

        // when
        ResultActions result = mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(reviewId.toString()))
            .andExpect(jsonPath("$.bookId").value(bookId.toString()))
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.content").value("굿 입니당~"))
            .andExpect(jsonPath("$.rating").value(4.2))
            .andExpect(jsonPath("$.bookTitle").value("테스트 책"))
            .andExpect(jsonPath("$.bookThumbnailUrl").value("http://image.url"))
            .andExpect(jsonPath("$.userNickname").value("테스터"))
            .andExpect(jsonPath("$.likeCount").value(0))
            .andExpect(jsonPath("$.commentCount").value(0))
            .andExpect(jsonPath("$.likedByMe").value(false))
            .andDo(print());
    }

    @Test
    void content_가_빈문자면_리뷰_생성에_실패한다() throws Exception {
        // given
        ReviewCreateRequest request = new ReviewCreateRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "", // 빈 내용
            3.0
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest())
            .andDo(print());
    }
}