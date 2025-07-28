package com.sprint.deokhugam.domain.reviewlike.repository;

import static com.sprint.deokhugam.fixture.BookFixture.createBookEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.reviewlike.entity.ReviewLike;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.config.JpaAuditingConfig;
import com.sprint.deokhugam.global.config.QueryDslConfig;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
@ActiveProfiles("test")
@DisplayName("ReviewLikeRepository 단위 테스트")
class ReviewLikeRepositoryTest {

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private TestEntityManager em;

    @BeforeEach
    void setUp() {
        // 유저 2명
        User user1 = em.persist(User.builder()
            .email("user1@test.com")
            .nickname("user1")
            .password("pass")
            .build());

        User user2 = em.persist(User.builder()
            .email("user2@test.com")
            .nickname("user2")
            .password("pass")
            .build());

        // 책 1권
        Book book = em.persist(createBookEntity("테스트 책", "작가", "설명",
            "출판사", LocalDate.of(2023, 1, 1), "9031245672313",
            null, 4.0, 3L));

        // 리뷰 2개
        Review review1 = em.persist(Review.builder()
            .content("리뷰 1")
            .likeCount(1L)
            .commentCount(2L)
            .rating(5)
            .book(book)
            .user(user1)
            .isDeleted(false)
            .build());

        Review review2 = em.persist(Review.builder()
            .content("리뷰 2")
            .likeCount(1L)
            .commentCount(2L)
            .rating(4)
            .book(book)
            .user(user2)
            .isDeleted(false)
            .build());

        // 좋아요 생성일: 2024-01-10, 2024-02-01
        Instant inRange = Instant.parse("2024-01-10T00:00:00Z");
        Instant outOfRange = Instant.parse("2024-02-01T00:00:00Z");

        ReviewLike like1 = new ReviewLike(review1, user2);
        ReviewLike like2 = new ReviewLike(review2, user1);

        em.persist(like1);
        em.persist(like2);

        em.flush();
        em.clear();

        em.getEntityManager().createQuery("UPDATE ReviewLike r SET r.createdAt = :createdAt WHERE r.id = :id")
            .setParameter("createdAt", inRange)
            .setParameter("id", like1.getId())
            .executeUpdate();

        em.getEntityManager().createQuery("UPDATE ReviewLike r SET r.createdAt = :createdAt WHERE r.id = :id")
            .setParameter("createdAt", outOfRange)
            .setParameter("id", like2.getId())
            .executeUpdate();
    }

    @Test
    void 특정_기간_동안_추가_된_좋아요_개수_테스트() {

        // given
        Instant start = Instant.parse("2024-01-01T00:00:00Z");
        Instant end = Instant.parse("2024-01-31T23:59:00Z");

        // when
        Map<UUID, Long> result = reviewLikeRepository.countByReviewIdBetween(start, end);

        // then
        assertEquals(1, result.size());
        assertTrue(result.values().contains(1L));
    }
}