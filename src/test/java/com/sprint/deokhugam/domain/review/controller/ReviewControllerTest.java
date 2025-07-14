package com.sprint.deokhugam.domain.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.sprint.deokhugam.domain.review.service.ReviewService;
import com.sprint.deokhugam.domain.user.entity.User;
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

    UUID bookId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String content = "굿 입니당~";
    double rating = 4.2;
    Instant now = Instant.now();

    @Test
    void 리뷰_생성_성공() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        Book book = mock(Book.class);
        User user = mock(User.class);
        Review savedReview = createReview(book, user);
        ReviewDto expectedDto = createDto(reviewId);
        given(reviewService.create(any())).willReturn(expectedDto);

        // when
        ResultActions result = mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest())));

        // then
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(reviewId.toString()))
            .andExpect(jsonPath("$.bookId").value(bookId.toString()))
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.content").value("굿 입니당~"))
            .andExpect(jsonPath("$.rating").value(4.2))
            .andExpect(jsonPath("$.bookTitle").value("테스트 책"))
            .andExpect(jsonPath("$.bookThumbnailUrl").value("http://image.url"))
            .andExpect(jsonPath("$.userNickname").value("리뷰어"))
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

    @Test
    void 리뷰_정보_조회에_성공하면_200응답을_반환한다() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        ReviewDto expectedDto = createDto(reviewId);
        given(reviewService.findById(reviewId)).willReturn(expectedDto);

        // when
        ResultActions result = mockMvc.perform(get("/api/reviews/{reviewId}", reviewId));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(reviewId.toString()))
            .andExpect(jsonPath("$.bookTitle").value("테스트 책"))
            .andExpect(jsonPath("$.userNickname").value("리뷰어"))
            .andDo(print());
    }

    @Test
    void 존재하지_않는_리뷰ID로_조회시_404를_반환한다() throws Exception {
        // given
        UUID notFoundId = UUID.randomUUID();
        given(reviewService.findById(notFoundId)).willThrow(new ReviewNotFoundException(notFoundId));

        // when
        ResultActions result = mockMvc.perform(get("/api/reviews/{reviewId}", notFoundId));

        // then
        result.andExpect(status().isNotFound())
            .andDo(print());
    }


    private ReviewCreateRequest createRequest() {
        return new ReviewCreateRequest(bookId, userId, content, rating);
    }

    private Review createReview(Book book, User user) {
        return new Review(rating, content, book, user);
    }

    private ReviewDto createDto(UUID reviewId) {
        return ReviewDto.builder()
            .id(reviewId)
            .bookId(bookId)
            .bookTitle("테스트 책")
            .bookThumbnailUrl("http://image.url")
            .userId(userId)
            .userNickname("리뷰어")
            .content(content)
            .rating(rating)
            .likeCount(0L)
            .commentCount(0L)
            .likedByMe(false)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }
}