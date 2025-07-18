package com.sprint.deokhugam.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.review.dto.request.ReviewGetRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.config.JpaAuditingConfig;
import com.sprint.deokhugam.global.config.QueryDslConfig;
import com.sprint.deokhugam.global.exception.InvalidTypeException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
@TestPropertySource(properties = "spring.sql.init.mode=never")
@DisplayName("ReviewRepository 단위 테스트")
@ActiveProfiles("dev")
public class CustomReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private TestEntityManager em;
    private List<Review> mockReviews;

    @BeforeEach
    void 초기_설정() {
        // 공통 시간
        Instant createdAt1 = Instant.parse("2025-01-02T00:00:00Z");
        Instant createdAt2 = Instant.parse("2025-01-03T00:00:00Z");
        Instant createdAt3 = Instant.parse("2025-01-04T00:00:00Z");

        /* review Entity 생성 */
        // ---------- [USER 1] ----------
        User user1 = User.builder()
            .email("user1@example.com")
            .nickname("유저1")
            .password("encryptedPwd1")
            .build();

        // ---------- [BOOK 1] ----------
        Book book1 = Book.builder()
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

        // ---------- [REVIEW 1] ----------
        Review review1 = Review.builder()
            .content("리뷰1")
            .rating(4)
            .likeCount(10L)
            .commentCount(12L)
            .isDeleted(true)
            .user(user1)
            .book(book1)
            .build();

        // ---------- [USER 2] ----------
        User user2 = User.builder()
            .email("user2@example.com")
            .nickname("유저2")
            .password("encryptedPwd2")
            .build();

        // ---------- [BOOK 2] ----------
        Book book2 = Book.builder()
            .title("책2")
            .author("저자2")
            .description("설명2")
            .publisher("출판사2")
            .publishedDate(LocalDate.parse("2021-05-20"))
            .isbn("222-2222222222")
            .thumbnailUrl("https://example.com/image2.jpg")
            .reviewCount(30L)
            .rating(3.8)
            .isDeleted(false)
            .build();

        // ---------- [REVIEW 2] ----------
        Review review2 = Review.builder()
            .content("리뷰2")
            .rating(3)
            .likeCount(382L)
            .commentCount(2L)
            .isDeleted(false)
            .user(user2)
            .book(book2)
            .build();

        // ---------- [USER 3] ----------
        User user3 = User.builder()
            .email("user3@example.com")
            .nickname("유저3")
            .password("encryptedPwd3")
            .build();

        // ---------- [BOOK 3] ----------
        Book book3 = Book.builder()
            .title("책3")
            .author("저자3")
            .description("설명3")
            .publisher("출판사3")
            .publishedDate(LocalDate.parse("2020-12-15"))
            .isbn("333-3333333333")
            .thumbnailUrl("https://example.com/image3.jpg")
            .reviewCount(21L)
            .rating(4.2)
            .isDeleted(false)
            .build();

        // ---------- [REVIEW 3] ----------
        Review review3 = Review.builder()
            .content("리뷰3")
            .rating(0)
            .likeCount(77L)
            .commentCount(6L)
            .isDeleted(false)
            .user(user3)
            .book(book3)
            .build();

        em.persist(user1);
        em.persist(user2);
        em.persist(user3);
        em.persist(book1);
        em.persist(book2);
        em.persist(book3);
        em.persist(review1);
        em.persist(review2);
        em.persist(review3);
        em.flush();  // DB 반영
        em.clear();  // 영속성 컨텍스트 초기화
        mockReviews = List.of(review1, review2, review3);
    }

    @Test
    void 키워드를_포함하여_리뷰를_전체조회한다() throws Exception {
        //given
        ReviewGetRequest request = new ReviewGetRequest(null, null, "유저2", null, null, 2,
            "createdAt", "DESC");

        //when
        List<Review> result = reviewRepository.findAll(request);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("리뷰2");
    }

    @Test
    void userId가_일치하는_리뷰를_전체조회한다() throws Exception {
        // given
        UUID userId = reviewRepository.findAll().get(0).getUser().getId();
        ReviewGetRequest request = new ReviewGetRequest(userId, null, null, null, null, 10,
            "createdAt", "DESC");
        // when
        List<Review> result = reviewRepository.findAll(request);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(userId);
    }

    @Test
    void bookId가_일치하는_리뷰를_전체조회한다() throws Exception {
        // given
        UUID bookId = reviewRepository.findAll().get(1).getBook().getId();
        ReviewGetRequest request = new ReviewGetRequest(null, bookId, null, null, null, 10,
            "createdAt", "DESC");
        // when
        List<Review> result = reviewRepository.findAll(request);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBook().getId()).isEqualTo(bookId);
    }

    /*pagination*/
    @Test
    void 최신순_오름차순으로_다음페이지_리뷰를_전체조회한다() throws Exception {
        // given
        Instant after = Instant.parse("2025-01-02T00:00:00Z");
        ReviewGetRequest request = new ReviewGetRequest(null, null, null, after, after, 10,
            "createdAt", "DESC");
        // when
        List<Review> result = reviewRepository.findAll(request);

        // then
        assertThat(result)
            .extracting(r -> r.getCreatedAt())
            .allMatch(t -> t.isAfter(after));
    }

    @Test
    void 최신순으로_조회하지만_cursor값이_datetime타입이_아니라면_400에러를_반환한다() throws Exception {
        // given
        Instant after = Instant.parse("2025-01-02T00:00:00Z");
        ReviewGetRequest request = new ReviewGetRequest(null, null, null, "1", after, 10,
            "createdAt", "ASC");

        // when
        Throwable thrown = catchThrowable(() -> reviewRepository.findAll(request));

        // then
        assertThat(thrown)
            .isInstanceOf(InvalidTypeException.class);
    }

    @Test
    void 평점순_오름차순으로_다음페이지_리뷰를_전체조회한다() throws Exception {
        // given
        Instant after = Instant.parse("2025-01-02T00:00:00Z");
        ReviewGetRequest request = new ReviewGetRequest(null, null, null, "1", after, 10,
            "rating", "ASC");

        // when
        List<Review> result = reviewRepository.findAll(request);

        // then
        assertThat(result).isSortedAccordingTo(
            Comparator.comparingDouble(Review::getRating)
        );

        assertThat(result)
            .allSatisfy(r -> assertThat(r.getRating()).isGreaterThanOrEqualTo(1));
    }

    @Test
    void 평점순으로_조회하지만_cursor값이_Integer타입이_아니라면_400에러를_반환한다() throws Exception {
        // given
        Instant after = Instant.parse("2025-01-02T00:00:00Z");
        ReviewGetRequest request = new ReviewGetRequest(null, null, null, "invalid", after, 10,
            "rating", "ASC");

        // when
        Throwable thrown = catchThrowable(() -> reviewRepository.findAll(request));

        // then
        assertThat(thrown)
            .isInstanceOf(InvalidTypeException.class);
    }


    @Test
    void 총_리뷰갯수_반환() throws Exception {
        // given
        ReviewGetRequest request = new ReviewGetRequest(null, null, null, null, null, 10,
            "createdAt", "ASC");
        // when
        Long result = reviewRepository.countAllByFilterCondition(request);

        // then
        assertThat(result).isEqualTo(2);
    }


    @Test
    void userId가_일치하는_총_리뷰갯수_반환() throws Exception {
        // given
        UUID userId = reviewRepository.findAll().get(0).getUser().getId();
        ReviewGetRequest request = new ReviewGetRequest(userId, null, null, null, null, 10,
            "createdAt", "DESC");
        // when
        Long result = reviewRepository.countAllByFilterCondition(request);

        // then
        assertThat(result).isEqualTo(1);
    }

    @Test
    void 리뷰를_하드_삭제할때_해당하는_리뷰가_이미_소프트_삭제가_되어있어야_한다() throws Exception {
        // given
        UUID reviewId = mockReviews.get(0).getId();

        // when
        Optional<Review> result = reviewRepository.findDeletedById(reviewId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getIsDeleted()).isEqualTo(true);
    }

    @Test
    void 리뷰를_하드_삭제할때_해당하는_리뷰가_이미_소프트_삭제가_되어있지않으면_null을_반환한다() throws Exception {
        // given
        UUID reviewId = mockReviews.get(1).getId();

        // when
        Optional<Review> result = reviewRepository.findDeletedById(reviewId);

        // then
        assertThat(result).isEmpty();
    }
}
