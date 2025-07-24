package com.sprint.deokhugam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("Review 통합 테스트")
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReviewIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    private User testUser;
    private Book testBook;
    private Review testReview;

    @BeforeEach
    void 초기_데이터_설정() {
        // 테스트 사용자 생성
        testUser = User.builder()
            .email("test@example.com")
            .nickname("쪼쪼")
            .password("password123!")
            .build();
        testUser = userRepository.save(testUser);

        // 테스트 도서 생성
        testBook = Book.builder()
            .title("테스트 도서")
            .author("테스트 저자")
            .description("테스트 설명")
            .publisher("테스트 출판사")
            .publishedDate(LocalDate.of(2023, 1, 1))
            .isbn("9780000000000")
            .thumbnailUrl("https://example.com/thumbnail.jpg")
            .reviewCount(0L)
            .rating(0.0)
            .isDeleted(false)
            .build();
        testBook = bookRepository.save(testBook);

        // 테스트 리뷰 생성
        testReview = Review.builder()
            .content("기존 리뷰 내용")
            .rating(4)
            .likeCount(0L)
            .commentCount(0L)
            .isDeleted(false)
            .user(testUser)
            .book(testBook)
            .build();
        testReview = reviewRepository.save(testReview);
    }

    @Test
    void 리뷰_생성_통합_테스트() throws Exception {
        // given
        User newUser = User.builder()
            .email("newzzo@example.com")
            .nickname("쪼쪼2")
            .password("password123!")
            .build();
        newUser = userRepository.save(newUser);
        ReviewCreateRequest request = new ReviewCreateRequest(
            testBook.getId(),
            newUser.getId(),
            "따봉 책!",
            5
        );
        
        // when
        ResultActions result = mockMvc.perform(post("/api/reviews")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("따봉 책!"))
            .andExpect(jsonPath("$.rating").value(5))
            .andExpect(jsonPath("$.bookTitle").value("테스트 도서"))
            .andExpect(jsonPath("$.userNickname").value("쪼쪼2"))
            .andDo(print());
        List<Review> reviews = reviewRepository.findAll();
        assertThat(reviews).hasSize(2);
        Review savedReview = reviews.stream()
            .filter(r -> r.getContent().equals("따봉 책!"))
            .findFirst()
            .orElseThrow();
        assertThat(savedReview.getRating()).isEqualTo(5);
        assertThat(savedReview.getUser().getId()).isEqualTo(newUser.getId());
        assertThat(savedReview.getBook().getId()).isEqualTo(testBook.getId());
    }

    @Test
    void 리뷰_조회_통합_테스트() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/reviews/{reviewId}", testReview.getId())
            .header("Deokhugam-Request-User-ID", testUser.getId()));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(testReview.getId().toString()))
            .andExpect(jsonPath("$.content").value("기존 리뷰 내용"))
            .andExpect(jsonPath("$.rating").value(4))
            .andExpect(jsonPath("$.bookTitle").value("테스트 도서"))
            .andExpect(jsonPath("$.userNickname").value("쪼쪼"))
            .andExpect(jsonPath("$.likedByMe").value(false))
            .andDo(print());
    }

    @Test
    void 리뷰_수정_통합_테스트() throws Exception {
        // given
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
            "수정된 리뷰 내용",
            2
        );

        // when
        ResultActions result = mockMvc.perform(patch("/api/reviews/{reviewId}", testReview.getId())
            .header("Deokhugam-Request-User-ID", testUser.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("수정된 리뷰 내용"))
            .andExpect(jsonPath("$.rating").value(2))
            .andDo(print());
        Optional<Review> updatedReview = reviewRepository.findById(testReview.getId());
        assertThat(updatedReview).isPresent();
        assertThat(updatedReview.get().getContent()).isEqualTo("수정된 리뷰 내용");
        assertThat(updatedReview.get().getRating()).isEqualTo(2);
    }

    @Test
    void 리뷰_소프트_삭제_통합_테스트() throws Exception {
        // when
        ResultActions result = mockMvc.perform(delete("/api/reviews/{reviewId}", testReview.getId())
            .header("Deokhugam-Request-User-ID", testUser.getId()));

        // then
        result.andExpect(status().isNoContent())
            .andDo(print());
        Optional<Review> deletedReview = reviewRepository.findById(testReview.getId());
        assertThat(deletedReview).isPresent();
        assertThat(deletedReview.get().getIsDeleted()).isTrue();
    }

    @Test
    void 리뷰_하드_삭제_통합_테스트() throws Exception {
        // given
        testReview.softDelete();
        reviewRepository.save(testReview);

        // when
        ResultActions result = mockMvc.perform(delete("/api/reviews/{reviewId}/hard", testReview.getId())
            .header("Deokhugam-Request-User-ID", testUser.getId()));

        // then
        result.andExpect(status().isNoContent())
            .andDo(print());
        Optional<Review> hardDeletedReview = reviewRepository.findById(testReview.getId());
        assertThat(hardDeletedReview).isEmpty();
    }

    @Test
    void 권한_없는_사용자_리뷰_수정_시도_통합_테스트() throws Exception {
        // given
        User unauthorizedUser = User.builder()
            .email("unauthorized@example.com")
            .nickname("권한없는유저")
            .password("password123!")
            .build();
        unauthorizedUser = userRepository.save(unauthorizedUser);
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(
            "수정 시도",
            5
        );

        // when
        ResultActions result = mockMvc.perform(patch("/api/reviews/{reviewId}", testReview.getId())
            .header("Deokhugam-Request-User-ID", unauthorizedUser.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)));

        // then
        result.andExpect(status().isForbidden()).andDo(print());
        Optional<Review> unchangedReview = reviewRepository.findById(testReview.getId());
        assertThat(unchangedReview).isPresent();
        assertThat(unchangedReview.get().getContent()).isEqualTo("기존 리뷰 내용");
        assertThat(unchangedReview.get().getRating()).isEqualTo(4);
    }

    @Test
    void 존재하지_않는_리뷰_조회_통합_테스트() throws Exception {
        // given
        UUID nonExistentReviewId = UUID.randomUUID();

        // when
        ResultActions result = mockMvc.perform(get("/api/reviews/{reviewId}", nonExistentReviewId)
            .header("Deokhugam-Request-User-ID", testUser.getId()));

        // then
        result.andExpect(status().isNotFound())
            .andDo(print());
    }

    @Test
    void 중복_리뷰_생성_시도_통합_테스트() throws Exception {
        // given
        ReviewCreateRequest duplicateRequest = new ReviewCreateRequest(
            testBook.getId(),
            testUser.getId(),
            "중복 리뷰",
            3
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/reviews")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(duplicateRequest)));

        // then
        result.andExpect(status().isConflict())
            .andDo(print());
        List<Review> reviews = reviewRepository.findAll();
        assertThat(reviews).hasSize(1);
    }

    @Test
    void 유효하지_않은_입력값_리뷰_생성_통합_테스트() throws Exception {
        // given
        ReviewCreateRequest invalidRequest = new ReviewCreateRequest(
            testBook.getId(),
            testUser.getId(),
            "", // 빈 내용
            6   // 유효하지 않은 평점 (1-5 범위를 벗어남)
        );

        // when
        ResultActions result = mockMvc.perform(post("/api/reviews")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)));

        // then
        result.andExpect(status().isBadRequest())
            .andDo(print());
        List<Review> reviews = reviewRepository.findAll();
        assertThat(reviews).hasSize(1);
    }

    @Test
    @DisplayName("통합 테스트: 닉네임으로 키워드 검색")
    void 닉네임_키워드_검색_리뷰_조회_통합_테스트() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/reviews")
            .param("keyword", "쪼쪼")
            .param("requestUserId", testUser.getId().toString())
            .header("Deokhugam-Request-User-ID", testUser.getId()));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].userNickname").value("쪼쪼"))
            .andExpect(jsonPath("$.content[0].content").value("기존 리뷰 내용"))
            .andDo(print());
    }

}
