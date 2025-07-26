package com.sprint.deokhugam.domain.popularreview.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import com.sprint.deokhugam.global.config.JpaAuditingConfig;
import com.sprint.deokhugam.global.config.QueryDslConfig;
import com.sprint.deokhugam.global.enums.PeriodType;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
@ActiveProfiles("test")
public class  PopularReviewRepositoryImplTest {

    @Autowired
    private PopularReviewRepositoryImpl popularReviewRepositoryImpl;

    @Autowired
    private PopularReviewRepository popularReviewRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private EntityManager em;

    private User user1, user2;
    private Book book1, book2;
    private Review review1, review2, review3;
    private PopularReview popularReview1, popularReview2, popularReview3;

    @BeforeEach
    void setUp() {
        // 기본 데이터 생성
        user1 = userRepository.save(User.builder()
            .email("user1@test.com")
            .nickname("유저1")
            .password("pw")
            .build());

        user2 = userRepository.save(User.builder()
            .email("user2@test.com")
            .nickname("유저2")
            .password("pw")
            .build());

        book1 = bookRepository.save(Book.builder()
            .title("테스트 책1")
            .author("저자1")
            .publisher("출판사1")
            .description("테스트 책1 설명")
            .isbn("1234567890")
            .publishedDate(LocalDate.now())
            .thumbnailUrl("http://example.com/book1.jpg")
            .rating(4.5)
            .reviewCount(0L)
            .isDeleted(false)
            .build());

        book2 = bookRepository.save(Book.builder()
            .title("테스트 책2")
            .author("저자2")
            .publisher("출판사2")
            .description("테스트 책2 설명")
            .isbn("0987654321")
            .publishedDate(LocalDate.now())
            .thumbnailUrl("http://example.com/book2.jpg")
            .rating(4.0)
            .reviewCount(0L)
            .isDeleted(false)
            .build());

        review1 = reviewRepository.save(Review.builder()
            .user(user1)
            .book(book1)
            .content("리뷰1")
            .rating(5)
            .likeCount(10L)
            .commentCount(5L)
            .isDeleted(false)
            .build());

        review2 = reviewRepository.save(Review.builder()
            .user(user2)
            .book(book2)
            .content("리뷰2")
            .rating(4)
            .likeCount(3L)
            .commentCount(2L)
            .isDeleted(false)
            .build());

        // 삭제된 리뷰 (필터링 테스트용)
        review3 = reviewRepository.save(Review.builder()
            .user(user1)
            .book(book2)
            .content("삭제된 리뷰")
            .rating(3)
            .likeCount(1L)
            .commentCount(1L)
            .isDeleted(true)
            .build());

        // PopularReview 데이터 생성
        popularReview1 = popularReviewRepository.save(PopularReview.builder()
            .review(review1)
            .period(PeriodType.DAILY)
            .rank(1L)
            .score(8.5)
            .likeCount(10L)
            .commentCount(5L)
            .build());

        popularReview2 = popularReviewRepository.save(PopularReview.builder()
            .review(review2)
            .period(PeriodType.DAILY)
            .rank(2L)
            .score(4.4)
            .likeCount(3L)
            .commentCount(2L)
            .build());

        popularReview3 = popularReviewRepository.save(PopularReview.builder()
            .review(review3)
            .period(PeriodType.WEEKLY)
            .rank(1L)
            .score(2.0)
            .likeCount(1L)
            .commentCount(1L)
            .build());
        em.flush();
        em.clear();
    }

    @Test
    void cursor와_after가_모두_null_일때_기본조회에_성공한다() {
        // when
        List<PopularReview> result = popularReviewRepositoryImpl.findByPeriodWithCursor(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            null,
            null,
            10
        );

        // then
        assertThat(result).hasSize(2); // review1, review2 (삭제된 리뷰 제외)
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(1).getRank()).isEqualTo(2L);
        assertThat(result.get(0).getReview().getUser().getNickname()).isEqualTo("유저1");
        assertThat(result.get(1).getReview().getUser().getNickname()).isEqualTo("유저2");
    }

    @Test
    void 빈_cursor_일때_기본조회에_성공한다() {
        // when
        List<PopularReview> result = popularReviewRepositoryImpl.findByPeriodWithCursor(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            "",
            null,
            10
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(1).getRank()).isEqualTo(2L);
    }

    @Test
    void cursor가_존재하고_after가_null_일때_순위대로_조회된다() {
        // when
        List<PopularReview> result = popularReviewRepositoryImpl.findByPeriodWithCursor(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            "1",
            null,
            10
        );

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void 순위가_같으면_after로_생성시간을_조건으로_조회된다_ASC() {
        // given
        String cursor = "1";
        Instant after = popularReview1.getCreatedAt().plusSeconds(1);

        // when
        List<PopularReview> result = popularReviewRepositoryImpl.findByPeriodWithCursor(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            cursor,
            after,
            10
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRank()).isEqualTo(2L);
    }

    @Test
    void 순위가_같으면_after로_생성시간을_조건으로_조회된다_DESC() {
        // given
        String cursor = "2"; // rank 2 이전
        Instant after = popularReview2.getCreatedAt().minusSeconds(1);

        // when
        List<PopularReview> result = popularReviewRepositoryImpl.findByPeriodWithCursor(
            PeriodType.DAILY,
            Sort.Direction.DESC,
            cursor,
            after,
            10
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRank()).isEqualTo(1L);
    }

    @Test
    void 기간별_조회에_성공한다_DAILY() {
        // when
        List<PopularReview> result = popularReviewRepositoryImpl.findByPeriodWithCursor(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            null,
            null,
            10
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPeriod()).isEqualTo(PeriodType.DAILY);
    }

    @Test
    void 삭제된_리뷰는_조회되지_않는다() {
        // when
        List<PopularReview> result = popularReviewRepositoryImpl.findByPeriodWithCursor(
            PeriodType.WEEKLY,
            Sort.Direction.ASC,
            null,
            null,
            10
        );

        // then
        assertThat(result).hasSize(0);
    }

    @Test
    void 오름차순_순위로_조회할_수_있다() {
        // when
        List<PopularReview> result = popularReviewRepositoryImpl.findByPeriodWithCursor(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            null,
            null,
            10
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isLessThan(result.get(1).getRank());
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(1).getRank()).isEqualTo(2L);
    }

    @Test
    void 내림차순_순위로_조회할_수_있다() {
        // when
        List<PopularReview> result = popularReviewRepositoryImpl.findByPeriodWithCursor(
            PeriodType.DAILY,
            Sort.Direction.DESC,
            null,
            null,
            10
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isGreaterThan(result.get(1).getRank());
        assertThat(result.get(0).getRank()).isEqualTo(2L);
        assertThat(result.get(1).getRank()).isEqualTo(1L);
    }

    @Test
    void cursor가_숫자가_아닐경우_NumberFormatException를_발생시킨다() {
        // given
        String invalidCursor = "invalid_cursor";

        // when
        Throwable thrown = catchThrowable(() ->
            popularReviewRepositoryImpl.findByPeriodWithCursor(
                PeriodType.DAILY,
                Sort.Direction.ASC,
                invalidCursor,
                Instant.now(),
                10
            )
        );

        // then
        assertThat(thrown).isInstanceOf(NumberFormatException.class);
    }

    @Test
    void after가_Instant형식이_아닐경우_DateTimeParseException를_발생시킨다() {
        // given
        String cursor = "1";
        Instant invalidAfter = mock(Instant.class);
        when(invalidAfter.toString()).thenReturn("invalid_date_format");

        // when
        Throwable thrown = catchThrowable(() ->
            popularReviewRepositoryImpl.findByPeriodWithCursor(
                PeriodType.DAILY,
                Sort.Direction.ASC,
                cursor,
                invalidAfter,
                10
            )
        );

        // then
        assertThat(thrown).isInstanceOf(DateTimeParseException.class);
    }

    @Test
    void rank가_같으면_createdAt_기준으로_정렬된다() {
        // given
        popularReviewRepository.save(PopularReview.builder()
            .review(review2)
            .period(PeriodType.DAILY)
            .rank(1L)
            .score(7.0)
            .likeCount(5L)
            .commentCount(3L)
            .build());
        em.flush();
        em.clear();
        String cursor = "1";
        Instant after = popularReview1.getCreatedAt().minusSeconds(1);

        // when
        List<PopularReview> result = popularReviewRepositoryImpl.findByPeriodWithCursor(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            cursor,
            after,
            10
        );

        // then
        assertThat(result.size()).isGreaterThanOrEqualTo(1);
        result.forEach(entity -> {
            assertThat(
                entity.getRank() > 1L ||
                    (entity.getRank().equals(1L) && entity.getCreatedAt().isAfter(after))
            ).isTrue();
        });
    }

    @Test
    void fetchJoin으로_연관_엔티티가_함께_조회된다() {
        // when
        List<PopularReview> result = popularReviewRepositoryImpl.findByPeriodWithCursor(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            null,
            null,
            10
        );

        // then
        assertThat(result).hasSize(2);
        PopularReview popularReview = result.get(0);
        assertThat(popularReview.getReview()).isNotNull();
        assertThat(popularReview.getReview().getContent()).isNotNull();
        assertThat(popularReview.getReview().getBook()).isNotNull();
        assertThat(popularReview.getReview().getBook().getTitle()).isNotNull();
        assertThat(popularReview.getReview().getBook().getThumbnailUrl()).isNotNull();
        assertThat(popularReview.getReview().getUser()).isNotNull();
        assertThat(popularReview.getReview().getUser().getNickname()).isNotNull();
    }
}