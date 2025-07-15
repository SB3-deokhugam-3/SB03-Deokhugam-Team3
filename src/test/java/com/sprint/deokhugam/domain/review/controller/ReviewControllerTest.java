package com.sprint.deokhugam.domain.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.sprint.deokhugam.domain.review.dto.request.ReviewGetRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.sprint.deokhugam.domain.review.service.ReviewService;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.domain.user.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ReviewController.class)
@DisplayName("ReviewController 슬라이스 테스트")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;
    private CursorPageResponse<ReviewDto> mockResponse;
    private List<ReviewDto> mockReviews;

    @BeforeEach
    void 초기_설정() {
        ReviewDto review1 = ReviewDto.builder()
            .id(UUID.fromString("cea1a965-2817-4431-90e3-e5701c70d43d"))
            .bookId(UUID.fromString("f6601c1d-c9b9-4ae1-a7aa-b4345921f4ca"))
            .userId(UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b"))
            .bookTitle("책1")
            .bookThumbnailUrl("https://example.com/image1.jpg")
            .userNickname("유저1")
            .content("리뷰1")
            .likeCount(10L)
            .commentCount(12L)
            .likedByMe(false)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        ReviewDto review2 = ReviewDto.builder()
            .id(UUID.fromString("044458f4-72a3-49aa-96f8-1a5160f444e2"))
            .bookId(UUID.fromString("17fede2c-5df9-4655-999c-03829265850e"))
            .userId(UUID.fromString("04e8e411-dd9c-451e-b03e-b393557b283e"))
            .bookTitle("책2")
            .bookThumbnailUrl("https://example.com/image2.jpg")
            .userNickname("유저2")
            .content("리뷰2")
            .likeCount(382L)
            .commentCount(2L)
            .likedByMe(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        mockReviews = List.of(review1, review2);
        mockResponse = new CursorPageResponse<>(
            mockReviews,
            null,
            null,
            mockReviews.size(),
            (long) mockReviews.size(),
            false
        );
    }


    UUID bookId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String content = "굿 입니당~";
    double rating = 4.2;
    Instant now = Instant.now();

    @Test
    void 리뷰_생성_요청시_201응답을_반환한다() throws Exception {
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
    void content_가_빈문자면_400에러를_반환한다() throws Exception {
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
    void 리뷰를_전체조회하면_200을_반환한다() throws Exception {
        //given
        given(reviewService.findAll(any(ReviewGetRequest.class), any(UUID.class))).willReturn(
            mockResponse);

        //when
        ResultActions result = mockMvc.perform(
            get("/api/reviews")
                .param("requestUserId", "cea1a965-2817-4431-90e3-e5701c70d43d")
                .header("Deokhugam-Request-User-ID", "cea1a965-2817-4431-90e3-e5701c70d43d")
        );

        //then
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.content[0].userNickname").value("유저1"))
            .andExpect(jsonPath("$.content[0].content").value("리뷰1"));
    }

    @Test
    void requestId가_없이_전체조회하면_400_에러를__반환한다() throws Exception {
        //given
        given(reviewService.findAll(any(ReviewGetRequest.class), any(UUID.class))).willReturn(
            mockResponse);

        //when
        ResultActions result = mockMvc.perform(
            get("/api/reviews")
                .header("Deokhugam-Request-User-ID", "cea1a965-2817-4431-90e3-e5701c70d43d")
        );

        //then
        result.andExpect(status().isBadRequest());
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