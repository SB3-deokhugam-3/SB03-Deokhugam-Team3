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
import java.util.UUID;
import org.assertj.core.api.Assertions;
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
//        ReflectionTestUtils.setField(user1, "id",
//            UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b"));
//        ReflectionTestUtils.setField(user1, "createdAt", createdAt1);
//        ReflectionTestUtils.setField(user1, "updatedAt", createdAt1);

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
//        ReflectionTestUtils.setField(book1, "id",
//            UUID.fromString("f6601c1d-c9b9-4ae1-a7aa-b4345921f4ca"));
//        ReflectionTestUtils.setField(book1, "createdAt", createdAt1);
//        ReflectionTestUtils.setField(book1, "updatedAt", createdAt1);

        // ---------- [REVIEW 1] ----------
        Review review1 = Review.builder()
            .content("리뷰1")
            .rating(4.0)
            .likeCount(10L)
            .commentCount(12L)
            .isDeleted(false)
            .user(user1)
            .book(book1)
            .build();
//        ReflectionTestUtils.setField(review1, "id",
//            UUID.fromString("cea1a965-2817-4431-90e3-e5701c70d43d"));
//        ReflectionTestUtils.setField(review1, "createdAt", createdAt1);
//        ReflectionTestUtils.setField(review1, "updatedAt", createdAt1);

        // ---------- [USER 2] ----------
        User user2 = User.builder()
            .email("user2@example.com")
            .nickname("유저2")
            .password("encryptedPwd2")
            .build();
//        ReflectionTestUtils.setField(user2, "id",
//            UUID.fromString("04e8e411-dd9c-451e-b03e-b393557b283e"));
//        ReflectionTestUtils.setField(user2, "createdAt", createdAt2);
//        ReflectionTestUtils.setField(user2, "updatedAt", createdAt2);

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
//        ReflectionTestUtils.setField(book2, "id",
//            UUID.fromString("17fede2c-5df9-4655-999c-03829265850e"));
//        ReflectionTestUtils.setField(book2, "createdAt", createdAt2);
//        ReflectionTestUtils.setField(book2, "updatedAt", createdAt2);

        // ---------- [REVIEW 2] ----------
        Review review2 = Review.builder()
            .content("리뷰2")
            .rating(3.0)
            .likeCount(382L)
            .commentCount(2L)
            .isDeleted(false)
            .user(user2)
            .book(book2)
            .build();
//        ReflectionTestUtils.setField(review2, "id",
//            UUID.fromString("044458f4-72a3-49aa-96f8-1a5160f444e2"));
//        ReflectionTestUtils.setField(review2, "createdAt", createdAt2);
//        ReflectionTestUtils.setField(review2, "updatedAt", createdAt2);

        // ---------- [USER 3] ----------
        User user3 = User.builder()
            .email("user3@example.com")
            .nickname("유저3")
            .password("encryptedPwd3")
            .build();
//        ReflectionTestUtils.setField(user3, "id",
//            UUID.fromString("92b2771b-59ea-420f-87b0-eafe16ec4321"));
//        ReflectionTestUtils.setField(user3, "createdAt", Instant.parse("2025-01-04T00:00:00Z"));
//        ReflectionTestUtils.setField(user3, "updatedAt", Instant.parse("2025-01-04T00:00:00Z"));

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
//        ReflectionTestUtils.setField(book3, "id",
//            UUID.fromString("7c315598-cdbe-491b-a2a7-36b6f1fc9473"));
//        ReflectionTestUtils.setField(book3, "createdAt", Instant.parse("2025-01-04T00:00:00Z"));
//        ReflectionTestUtils.setField(book3, "updatedAt", Instant.parse("2025-01-04T00:00:00Z"));

        // ---------- [REVIEW 3] ----------
        Review review3 = Review.builder()
            .content("리뷰3")
            .rating(0.0)
            .likeCount(77L)
            .commentCount(6L)
            .isDeleted(false)
            .user(user3)
            .book(book3)
            .build();
//        ReflectionTestUtils.setField(review3, "id",
//            UUID.fromString("b99bc315-2400-4ff6-8891-9a42f4c31bc4"));
//        ReflectionTestUtils.setField(review3, "createdAt", Instant.parse("2025-01-04T00:00:00Z"));
//        ReflectionTestUtils.setField(review3, "updatedAt", Instant.parse("2025-01-04T00:00:00Z"));

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
    }

    @Test
    void 키워드를_포함하여_리뷰를_전체조회한다() throws Exception {
        //given
        ReviewGetRequest request = ReviewGetRequest.builder()
            .orderBy("createdAt")
            .direction("DESC")
            .keyword("유저1") // 닉네임
            .limit(2)
            .build();

        //when
        List<Review> result = reviewRepository.findAll(request);

        //then
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.get(0).getContent()).isEqualTo("리뷰1");
    }

    @Test
    void userId가_일치하는_리뷰를_전체조회한다() throws Exception {
        // given
        UUID userId = reviewRepository.findAll().get(0).getUser().getId();
        ReviewGetRequest request = ReviewGetRequest.builder()
            .orderBy("createdAt")
            .direction("DESC")
            .userId(userId)
            .limit(10)
            .build();

        // when
        List<Review> result = reviewRepository.findAll(request);

        // then
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.get(0).getUser().getId()).isEqualTo(userId);
    }

    @Test
    void bookId가_일치하는_리뷰를_전체조회한다() throws Exception {
        // given
        UUID bookId = reviewRepository.findAll().get(1).getBook().getId();
        ReviewGetRequest request = ReviewGetRequest.builder()
            .orderBy("createdAt")
            .direction("DESC")
            .bookId(bookId)
            .limit(10)
            .build();

        // when
        List<Review> result = reviewRepository.findAll(request);

        // then
        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.get(0).getBook().getId()).isEqualTo(bookId);
    }

    /*pagination*/
    @Test
    void 최신순_오름차순으로_다음페이지_리뷰를_전체조회한다() throws Exception {
        // given
        Instant after = Instant.parse("2025-01-02T00:00:00Z");
        ReviewGetRequest request = ReviewGetRequest.builder()
            .orderBy("createdAt")
            .direction("ASC")
            .cursor(after)
            .after(after)
            .limit(10)
            .build();

        // when
        List<Review> result = reviewRepository.findAll(request);

        // then
        Assertions.assertThat(result)
            .extracting(r -> r.getCreatedAt())
            .allMatch(t -> t.isAfter(after));
    }

    @Test
    void 최신순으로_조회하지만_cursor값이_datetime타입이_아니라면_400에러를_반환한다() throws Exception {
        // given
        Instant after = Instant.parse("2025-01-02T00:00:00Z");
        ReviewGetRequest request = ReviewGetRequest.builder()
            .orderBy("createdAt")
            .direction("ASC")
            .cursor("1.0")
            .after(after)
            .limit(10)
            .build();

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
        ReviewGetRequest request = ReviewGetRequest.builder()
            .orderBy("rating")
            .direction("ASC")
            .cursor("1.0")
            .after(after)
            .limit(10)
            .build();

        // when
        List<Review> result = reviewRepository.findAll(request);

        // then
        Assertions.assertThat(result).isSortedAccordingTo(
            Comparator.comparingDouble(Review::getRating)
        );

        Assertions.assertThat(result)
            .allSatisfy(r -> Assertions.assertThat(r.getRating()).isGreaterThanOrEqualTo(1.0));
    }

    @Test
    void 평점순으로_조회하지만_cursor값이_double타입이_아니라면_400에러를_반환한다() throws Exception {
        // given
        Instant after = Instant.parse("2025-01-02T00:00:00Z");
        ReviewGetRequest request = ReviewGetRequest.builder()
            .orderBy("rating")
            .direction("ASC")
            .cursor("invalid")
            .after(after)
            .limit(10)
            .build();

        // when
        Throwable thrown = catchThrowable(() -> reviewRepository.findAll(request));

        // then
        assertThat(thrown)
            .isInstanceOf(InvalidTypeException.class);
    }


    @Test
    void 총_리뷰갯수_반환() throws Exception {
        // given
        ReviewGetRequest request = ReviewGetRequest.builder()
            .orderBy("createdAt")
            .direction("ASC")
            .limit(10)
            .build();

        // when
        Long result = reviewRepository.countAllByFilterCondition(request);

        // then
        Assertions.assertThat(result).isEqualTo(3);
    }


    @Test
    void userId가_일치하는_총_리뷰갯수_반환() throws Exception {
        // given
        UUID userId = reviewRepository.findAll().get(0).getUser().getId();
        ReviewGetRequest request = ReviewGetRequest.builder()
            .orderBy("createdAt")
            .direction("DESC")
            .userId(userId)
            .limit(10)
            .build();

        // when
        Long result = reviewRepository.countAllByFilterCondition(request);

        // then
        Assertions.assertThat(result).isEqualTo(1);
    }
}
