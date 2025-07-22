package com.sprint.deokhugam.domain.popularreview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.book.storage.s3.S3Storage;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.domain.popularreview.repository.PopularReviewRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.mapper.ReviewMapper;
import com.sprint.deokhugam.domain.reviewlike.repository.ReviewLikeRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import com.sprint.deokhugam.global.enums.PeriodType;
import com.sun.jdi.request.DuplicateRequestException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("PopularReviewService 단위 테스트")
public class PopularReviewServiceTest {

    @Mock
    private PopularReviewRepository popularReviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private S3Storage s3Storage;

    @Mock
    private StepContribution contribution;

    @InjectMocks
    private PopularReviewService popularReviewService;
    private List<Review> mockReviews;

    @BeforeEach
    void 초기_설정() {

//
//        Review review2 = Review.builder()
//            .content("리뷰2")
//            .rating(0)
//            .likeCount(382L)
//            .commentCount(2L)
//            .isDeleted(false)
//            .user(user)
//            .book(book)
//            .build();
//        ReflectionTestUtils.setField(review2, "id",
//            UUID.fromString("044458f4-72a3-49aa-96f8-1a5160f444e2"));
//        ReflectionTestUtils.setField(review2, "createdAt", Instant.parse("2025-01-04T00:00:00Z"));
//
//        Review review3 = Review.builder()
//            .content("리뷰3")
//            .rating(0)
//            .likeCount(77L)
//            .commentCount(6L)
//            .isDeleted(false)
//            .user(user)
//            .book(book)
//            .build();
//        ReflectionTestUtils.setField(review3, "id",
//            UUID.fromString("b99bc315-2400-4ff6-8891-9a42f4c31bc4"));
//        ReflectionTestUtils.setField(review3, "createdAt", Instant.parse("2025-01-04T00:00:00Z"));
//
//        mockReviews = List.of(review1, review2, review3);
    }

    private PopularReview createPopularReview(Review review, String period, Long rank,
        double score) {
        PopularReview popularReview = PopularReview.builder()
            .review(review)
            .period(period)
            .rank(rank)
            .score(score)
            .commentCount(review.getCommentCount())
            .likeCount(review.getLikeCount())
            .build();

        return popularReview;
    }

    private Review createReview(Long commentCount, Long likeCount, String createdAt) {
        User user = User.builder()
            .email("user1@example.com")
            .nickname("유저1")
            .password("encryptedPwd1")
            .build();
        ReflectionTestUtils.setField(user, "id",
            UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b"));

        Book book = Book.builder()
            .title("책1")
            .author("저자1")
            .description("설명1")
            .publisher("출판사1")
            .publishedDate(LocalDate.parse("2022-01-01"))
            .isbn("111-1111111111")
            .thumbnailUrl("https://example.com/image1.jpg")
            .reviewCount(10L)
            .rating(4.5)
            .isDeleted(false)
            .build();
        ReflectionTestUtils.setField(book, "id",
            UUID.fromString("f6601c1d-c9b9-4ae1-a7aa-b4345921f4ca"));

        Review review = new Review(4, "이 책 따봉임", book, user);
        ReflectionTestUtils.setField(review, "likeCount", likeCount);
        ReflectionTestUtils.setField(review, "commentCount", commentCount);
        ReflectionTestUtils.setField(review, "createdAt", Instant.parse(createdAt));

        return review;
    }

    @Test
    void 오늘날짜로_데이터가_생성된게_없다면_에러를_반환하지_않는다() {
        //given
        given(popularReviewRepository.existsByCreatedAtBetween(any(Instant.class),
            any(Instant.class)))
            .willReturn(false);

        //when
        Executable executable = () -> popularReviewService.validateJobNotDuplicated(
            Instant.now());

        //then
        assertDoesNotThrow(executable);
    }

    @Test
    void 오늘날짜로_데이터가_생성된게_이미_있다면_DuplicateRequestException에러를_반환한다() {
        //given
        given(popularReviewRepository.existsByCreatedAtBetween(any(Instant.class),
            any(Instant.class)))
            .willReturn(true);

        //when
        Throwable thrown = catchThrowable(() -> popularReviewService.validateJobNotDuplicated(
            Instant.now()));

        //then
        assertThat(thrown)
            .isInstanceOf(DuplicateRequestException.class);
    }

    @Test
    void 전체기간_기준으로_인기리뷰를_저장한다() {
        //given
        Review review1 = createReview(1L, 1L, "2025-01-03T00:00:00Z");
        Review review2 = createReview(3L, 3L, "2025-06-03T00:00:00Z");
        Review review3 = createReview(2L, 2L, "2025-07-22T00:00:00Z");
        List<Review> totalReviews = List.of(
            review1,
            review2,
            review3
        );
        List<PopularReview> expectedPopularReviews = List.of(
            createPopularReview(review1, PeriodType.ALL_TIME.getValue(), 1L,
                review1.getCommentCount() * 0.7 + review1.getLikeCount() * 0.3),
            createPopularReview(review2, PeriodType.ALL_TIME.getValue(), 2L,
                review2.getCommentCount() * 0.7 + review2.getLikeCount() * 0.3),
            createPopularReview(review3, PeriodType.ALL_TIME.getValue(), 3L,
                review3.getCommentCount() * 0.7 + review3.getLikeCount() * 0.3)
        );

        // when
        List<PopularReview> result = popularReviewService.savePopularReviewsByPeriod(totalReviews,
            PeriodType.ALL_TIME,
            contribution);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(0).getScore()).isEqualTo(1L * 0.7 + 1L * 0.3);
        then(popularReviewRepository).should().saveAll(any());
        then(contribution).should().incrementWriteCount(expectedPopularReviews.size());
    }

    @Test
    void 월기간_기준으로_인기리뷰를_저장한다() {
        //given
        Review review1 = createReview(1L, 1L, "2025-07-03T00:00:00Z");
        Review notInPeriodReview = createReview(3L, 3L, "2025-06-01T00:00:00Z");
        Review review3 = createReview(2L, 2L, "2025-07-22T00:00:00Z");
        List<Review> totalReviews = List.of(
            review3,
            notInPeriodReview,
            review1
        );
        List<PopularReview> expectedPopularReviews = List.of(
            createPopularReview(review3, PeriodType.ALL_TIME.getValue(), 1L,
                review3.getCommentCount() * 0.7 + review3.getLikeCount() * 0.3),
            createPopularReview(review1, PeriodType.ALL_TIME.getValue(), 2L,
                review1.getCommentCount() * 0.7 + review1.getLikeCount() * 0.3)
        );

        // when
        List<PopularReview> result = popularReviewService.savePopularReviewsByPeriod(totalReviews,
            PeriodType.MONTHLY,
            contribution);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(0).getScore()).isEqualTo(
            review3.getCommentCount() * 0.7 + review3.getLikeCount() * 0.3);
        then(popularReviewRepository).should().saveAll(any());
        then(contribution).should().incrementWriteCount(expectedPopularReviews.size());
    }

    @Test
    void 주기간_기준으로_인기리뷰를_저장한다() {
        //given
        Review notInPeriodReview = createReview(1L, 1L, "2025-07-03T00:00:00Z");
        Review notInPeriodReview2 = createReview(3L, 3L, "2025-06-01T00:00:00Z");
        Review review3 = createReview(2L, 2L, "2025-07-22T00:00:00Z");
        List<Review> totalReviews = List.of(
            review3,
            notInPeriodReview,
            notInPeriodReview2
        );
        List<PopularReview> expectedPopularReviews = List.of(
            createPopularReview(review3, PeriodType.ALL_TIME.getValue(), 1L,
                review3.getCommentCount() * 0.7 + review3.getLikeCount() * 0.3)
        );

        // when
        List<PopularReview> result = popularReviewService.savePopularReviewsByPeriod(totalReviews,
            PeriodType.WEEKLY,
            contribution);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(0).getScore()).isEqualTo(
            review3.getCommentCount() * 0.7 + review3.getLikeCount() * 0.3);
        then(popularReviewRepository).should().saveAll(any());
        then(contribution).should().incrementWriteCount(expectedPopularReviews.size());

    }

    @Test
    void 일기간_기준으로_인기리뷰를_저장한다() {
        //given
        Review notInPeriodReview = createReview(1L, 1L, "2025-07-03T00:00:00Z");
        Review notInPeriodReview2 = createReview(3L, 3L, "2025-06-01T00:00:00Z");
        Review notInPeriodReview3 = createReview(2L, 2L, "2025-07-20T00:00:00Z");
        List<Review> totalReviews = List.of(
            notInPeriodReview3,
            notInPeriodReview,
            notInPeriodReview2
        );
        List<PopularReview> expectedPopularReviews = List.of();

        // when
        List<PopularReview> result = popularReviewService.savePopularReviewsByPeriod(totalReviews,
            PeriodType.DAILY,
            contribution);

        // then
        assertThat(result).hasSize(0);
        then(popularReviewRepository).should().saveAll(any());
        then(contribution).should().incrementWriteCount(expectedPopularReviews.size());


    }
}
