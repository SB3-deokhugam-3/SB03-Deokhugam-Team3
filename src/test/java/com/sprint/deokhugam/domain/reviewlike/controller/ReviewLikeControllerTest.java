package com.sprint.deokhugam.domain.reviewlike.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.reviewlike.dto.data.ReviewLikeDto;
import com.sprint.deokhugam.domain.reviewlike.service.ReviewLikeService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ReviewLikeController.class)
@DisplayName("ReviewLikeController 슬라이스 테스트")
@ActiveProfiles("test")
public class ReviewLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewLikeService reviewLikeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 리뷰_좋아요_생성시_200응답을_반환한다() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ReviewLikeDto reviewLikeDto = new ReviewLikeDto(reviewId, userId, true);
        given(reviewLikeService.toggleLike(reviewId, userId)).willReturn(reviewLikeDto);

        // when
        ResultActions result = mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId)
            .header("Deokhugam-Request-User-ID", userId)
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.liked").value(true))
            .andDo(print());
        then(reviewLikeService).should().toggleLike(reviewId, userId);
    }

    @Test
    void 리뷰_좋아요_삭제시에도_200응답을_반환한다() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ReviewLikeDto reviewLikeDto = new ReviewLikeDto(reviewId, userId, false);
        given(reviewLikeService.toggleLike(reviewId, userId)).willReturn(reviewLikeDto);

        // when
        ResultActions result = mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId)
            .header("Deokhugam-Request-User-ID", userId)
            .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.liked").value(false))
            .andDo(print());
        then(reviewLikeService).should().toggleLike(reviewId, userId);
    }

}
