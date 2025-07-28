package com.sprint.deokhugam.domain.popularreview.service;

import static com.sprint.deokhugam.fixture.ReviewFixture.book;
import static com.sprint.deokhugam.fixture.ReviewFixture.dto;
import static com.sprint.deokhugam.fixture.ReviewFixture.popularReview;
import static com.sprint.deokhugam.fixture.ReviewFixture.review;
import static com.sprint.deokhugam.fixture.ReviewFixture.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.global.storage.S3Storage;
import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.domain.popularreview.mapper.PopularReviewMapper;
import com.sprint.deokhugam.domain.popularreview.repository.PopularReviewRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.enums.PeriodType;
import com.sprint.deokhugam.global.exception.BatchAlreadyRunException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
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
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("PopularReviewService 단위 테스트")
public class PopularReviewServiceTest {

    @InjectMocks
    private PopularReviewServiceImpl popularReviewService;

    @Mock
    private PopularReviewRepository popularReviewRepository;

    @Mock
    private PopularReviewMapper popularReviewMapper;

    @Mock
    private S3Storage s3Storage;

    @Mock
    private StepContribution contribution;

    private User user1;
    private User user2;
    private Book book1;
    private Book book2;
    private Review review1;
    private Review review2;
    private Review review3;
    private PopularReview popularReview1;
    private PopularReview popularReview2;
    private PopularReview popularReview3;
    private PopularReviewDto popularReviewDto1;
    private PopularReviewDto popularReviewDto2;
    private PopularReviewDto popularReviewDto3;

    @BeforeEach
    void setup() {
        user1 = user("user1@test.com", "유저1");
        book1 = book("테스트 책1", "1234567890");
        review1 = review(user1, book1, "리뷰1", 10, 5, false);

        user2 = user("user1@test.com", "유저1");
        book2 = book("테스트 책2", "0987654321");
        review2 = review(user1, book1, "리뷰2", 3, 2, false);
        review3 = review(user1, book2, "삭제된 리뷰", 3, 1, true);

        popularReview1 = popularReview(review1, PeriodType.DAILY, 1L, 8.5);
        popularReview2 = popularReview(review2, PeriodType.DAILY, 2L, 4.4);
        popularReview3 = popularReview(review3, PeriodType.WEEKLY, 1L, 2.0);

        popularReviewDto1 = dto(popularReview1);
        popularReviewDto2 = dto(popularReview2);
        popularReviewDto3 = dto(popularReview3);
    }

    @Test
    void 오늘날짜로_데이터가_생성된게_없다면_에러를_반환하지_않는다() {
        //given
        for (PeriodType period : PeriodType.values()) {
            given(popularReviewRepository.countByPeriod(period))
                .willReturn(0L);
        }

        //when
        Executable executable = () -> popularReviewService.validateJobNotDuplicated(
            Instant.now());

        //then
        assertDoesNotThrow(executable);
    }

    @Test
    void 오늘날짜로_데이터가_생성된게_이미_있다면_BatchAlreadyRunException에러를_반환한다() {
        //given
        // 첫 번째로 체크되는 PeriodType에서만 1을 반환
        given(popularReviewRepository.countByPeriod(any(PeriodType.class)))
            .willReturn(1L);

        //when
        Throwable thrown = catchThrowable(() -> popularReviewService.validateJobNotDuplicated(
            Instant.now()));

        //then
        assertThat(thrown)
            .isInstanceOf(BatchAlreadyRunException.class);
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
            createPopularReview(review1, PeriodType.ALL_TIME, 1L,
                review1.getCommentCount() * 0.7 + review1.getLikeCount() * 0.3),
            createPopularReview(review2, PeriodType.ALL_TIME, 2L,
                review2.getCommentCount() * 0.7 + review2.getLikeCount() * 0.3),
            createPopularReview(review3, PeriodType.ALL_TIME, 3L,
                review3.getCommentCount() * 0.7 + review3.getLikeCount() * 0.3)
        );

        // when
        List<PopularReview> result = popularReviewService.savePopularReviewsByPeriod(totalReviews,
            PeriodType.ALL_TIME,
            contribution, Instant.now());

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
            createPopularReview(review3, PeriodType.ALL_TIME, 1L,
                review3.getCommentCount() * 0.7 + review3.getLikeCount() * 0.3),
            createPopularReview(review1, PeriodType.ALL_TIME, 2L,
                review1.getCommentCount() * 0.7 + review1.getLikeCount() * 0.3)
        );

        // when
        List<PopularReview> result = popularReviewService.savePopularReviewsByPeriod(totalReviews,
            PeriodType.MONTHLY, contribution, Instant.parse("2025-07-23T00:00:00Z"));

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(0).getScore()).isEqualTo(
            review3.getCommentCount() * 0.7 + review3.getLikeCount() * 0.3);
        then(popularReviewRepository).should().saveAll(any());
        then(contribution).should().incrementWriteCount(expectedPopularReviews.size());
    }

    @Test
    @DisplayName("주간 인기리뷰:7/18~7/24일 데이터를 가져온다(한국시간기준)")
    void 주기간_기준으로_인기리뷰를_저장한다() {
        //given
        Instant currentTime = Instant.parse("2025-07-24T15:05:00Z"); // 한국시간 7/25일 00:05
        Review notInPeriodReview1 = createReview(3L, 3L, "2025-06-01T00:00:00Z");
        Review notInPeriodReview2 = createReview(2L, 2L,
            "2025-07-17T14:49:00Z"); // 한국시간 7/17일 23:59
        Review notInPeriodReview3 = createReview(2L, 2L,
            "2025-07-25T15:00:00Z"); // 한국시간 7/26일 00:00

        Review review1 = createReview(1L, 1L, "2025-07-17T15:00:00Z");  // 한국시간 7/18일 00:00
        Review review2 = createReview(2L, 2L, "2025-07-24T14:49:00Z");  // 한국시간 7/24일 23:59
        Review reviewToday1 = createReview(3L, 3L, "2025-07-24T15:00:00Z");  // 한국시간 7/25일 00:00
        Review reviewToday2 = createReview(4L, 4L, "2025-07-25T14:59:59Z");  // 한국시간 7/25일 23:59

        List<Review> totalReviews = List.of(
            notInPeriodReview1,
            notInPeriodReview2,
            notInPeriodReview3,
            reviewToday2,
            reviewToday1,
            review2,
            review1
        );
        List<PopularReview> expectedPopularReviews = List.of(
            createPopularReview(review2, PeriodType.WEEKLY, 1L, 2.0),
            createPopularReview(review1, PeriodType.WEEKLY, 2L, 1.0)
        );

        // when
        List<PopularReview> result = popularReviewService.savePopularReviewsByPeriod(totalReviews,
            PeriodType.WEEKLY, contribution, currentTime);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(1).getRank()).isEqualTo(2L);
        assertThat(result.get(1).getScore()).isEqualTo(1.0);
        then(popularReviewRepository).should().saveAll(any());
        then(contribution).should().incrementWriteCount(expectedPopularReviews.size());

    }

    @Test
    @DisplayName("일간 인기리뷰:7/24일 데이터를 가져온다(한국시간기준)")
    void 일기간_기준으로_인기리뷰를_저장한다() {

        //given
        Instant currentTime = Instant.parse("2025-07-24T15:05:00Z"); // 한국시간 7/25일 00:05
        Review notInPeriodReview1 = createReview(3L, 3L, "2025-06-01T00:00:00Z");
        Review notInPeriodReview2 = createReview(2L, 2L,
            "2025-07-23T14:59:00Z"); // 한국시간 7/23일 23:59
        Review reviewYesterday1 = createReview(1L, 1L, "2025-07-23T15:00:00Z");  // 한국시간 7/24일 00:00
        Review reviewYesterday2 = createReview(2L, 2L, "2025-07-24T14:59:00Z");  // 한국시간 7/24일 23:59
        Review reviewToday1 = createReview(3L, 3L, "2025-07-24T15:00:00Z");  // 한국시간 7/25일 00:00
        Review reviewToday2 = createReview(4L, 4L, "2025-07-25T14:59:59Z");  // 한국시간 7/25일 23:59

        List<Review> totalReviews = List.of(
            notInPeriodReview1,
            notInPeriodReview2,
            reviewYesterday2,
            reviewYesterday1,
            reviewToday1,
            reviewToday2
        );
        List<PopularReview> expectedPopularReviews = List.of(
            createPopularReview(reviewYesterday2, PeriodType.DAILY, 1L, 2.0),
            createPopularReview(reviewYesterday1, PeriodType.DAILY, 2L, 1.0)
        );

        // when
        List<PopularReview> result = popularReviewService.savePopularReviewsByPeriod(totalReviews,
            PeriodType.DAILY, contribution, currentTime);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(0).getScore()).isEqualTo(2.0);
        assertThat(result.get(1).getRank()).isEqualTo(2L);
        assertThat(result.get(1).getScore()).isEqualTo(1.0);
        then(popularReviewRepository).should().saveAll(any());
        then(contribution).should().incrementWriteCount(expectedPopularReviews.size());
    }


    @Test
    void getPopularReviews_정상작동() {
        // given
        PeriodType period = PeriodType.DAILY;
        Sort.Direction direction = Sort.Direction.ASC;
        String cursor = null;
        Instant after = Instant.now();
        int limit = 2;
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
            .thenReturn(List.of(popularReview1, popularReview2, popularReview3)); // limit+1개 반환
        when(popularReviewMapper.toDto(eq(popularReview1), eq(s3Storage)))
            .thenReturn(popularReviewDto1);
        when(popularReviewMapper.toDto(eq(popularReview2), eq(s3Storage)))
            .thenReturn(popularReviewDto2);

        // when
        CursorPageResponse<PopularReviewDto> response = popularReviewService.getPopularReviews(
            period, direction, cursor, after, limit
        );

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.content()).containsExactly(popularReviewDto1, popularReviewDto2);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
        assertThat(response.nextAfter()).isEqualTo(popularReviewDto2.createdAt().toString());
    }

    @Test
    void getPopularReviews_hasNext가_false일때() {
        // given
        PeriodType period = PeriodType.DAILY;
        Sort.Direction direction = Sort.Direction.ASC;
        String cursor = null;
        Instant after = Instant.now();
        int limit = 3;
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
            .thenReturn(List.of(popularReview1, popularReview2));
        when(popularReviewMapper.toDto(eq(popularReview1), eq(s3Storage)))
            .thenReturn(popularReviewDto1);
        when(popularReviewMapper.toDto(eq(popularReview2), eq(s3Storage)))
            .thenReturn(popularReviewDto2);

        // when
        CursorPageResponse<PopularReviewDto> response = popularReviewService.getPopularReviews(
            period, direction, cursor, after, limit
        );

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    void getPopularReviews_빈_결과일때() {
        // given
        PeriodType period = PeriodType.DAILY;
        Sort.Direction direction = Sort.Direction.ASC;
        String cursor = null;
        Instant after = Instant.now();
        int limit = 2;
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
            .thenReturn(List.of());

        // when
        CursorPageResponse<PopularReviewDto> response = popularReviewService.getPopularReviews(
            period, direction, cursor, after, limit
        );

        // then
        assertThat(response.content()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextAfter()).isNull();
    }

    @Test
    void generateCursor_정상작동() {
        // given
        PeriodType period = PeriodType.DAILY;
        Sort.Direction direction = Sort.Direction.ASC;
        String cursor = null;
        Instant after = Instant.now();
        int limit = 1;

        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
            .thenReturn(List.of(popularReview1, popularReview2));
        when(popularReviewMapper.toDto(eq(popularReview1), eq(s3Storage)))
            .thenReturn(popularReviewDto1);

        // when
        CursorPageResponse<PopularReviewDto> response = popularReviewService.getPopularReviews(
            period, direction, cursor, after, limit
        );

        // then
        assertThat(response.nextCursor())
            .isEqualTo(String.valueOf(popularReviewDto1.rank()));
        assertThat(response.nextAfter())
            .isEqualTo(popularReviewDto1.createdAt().toString());
        assertThat(response.content())
            .containsExactly(popularReviewDto1);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void 커서_페이징이_순차적으로_동작한다() {
        // given - 첫 번째 페이지
        PeriodType period = PeriodType.DAILY;
        Sort.Direction direction = Sort.Direction.ASC;
        String cursor = null;
        Instant after = null;
        int limit = 1;
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
            .thenReturn(List.of(popularReview1, popularReview2));
        when(popularReviewMapper.toDto(eq(popularReview1), eq(s3Storage)))
            .thenReturn(popularReviewDto1);

        // when - 첫 번째 페이지 조회
        CursorPageResponse<PopularReviewDto> firstPage = popularReviewService.getPopularReviews(
            period, direction, cursor, after, limit
        );

        // then - 첫 번째 페이지 검증
        assertThat(firstPage.content()).hasSize(1);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.nextCursor()).isNotNull();

        // given - 두 번째 페이지 (첫 번째 페이지의 커서 사용)
        String nextCursor = firstPage.nextCursor();
        Instant nextAfter = Instant.parse(firstPage.nextAfter());
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(nextCursor), eq(nextAfter), eq(limit + 1)))
            .thenReturn(List.of(popularReview2)); // 마지막 페이지
        when(popularReviewMapper.toDto(eq(popularReview2), eq(s3Storage)))
            .thenReturn(popularReviewDto2);

        // when
        CursorPageResponse<PopularReviewDto> secondPage = popularReviewService.getPopularReviews(
            period, direction, nextCursor, nextAfter, limit
        );

        // then
        assertThat(secondPage.content()).hasSize(1);
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.nextCursor()).isNull();
    }

    @Test
    void mapper와_s3Storage_연동이_정상작동한다() {
        // given
        PeriodType period = PeriodType.DAILY;
        Sort.Direction direction = Sort.Direction.ASC;
        String cursor = null;
        Instant after = Instant.now();
        int limit = 1;
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
            .thenReturn(List.of(popularReview1));
        when(popularReviewMapper.toDto(eq(popularReview1), eq(s3Storage)))
            .thenReturn(popularReviewDto1);

        // when
        CursorPageResponse<PopularReviewDto> response = popularReviewService.getPopularReviews(
            period, direction, cursor, after, limit
        );

        // then
        assertThat(response.content()).hasSize(1);
        PopularReviewDto dto = response.content().get(0);
        assertThat(dto.bookThumbnailUrl()).isEqualTo("https://s3.example.com/converted.jpg");
    }

    private PopularReview createPopularReview(Review review, PeriodType period, Long rank,
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
}
