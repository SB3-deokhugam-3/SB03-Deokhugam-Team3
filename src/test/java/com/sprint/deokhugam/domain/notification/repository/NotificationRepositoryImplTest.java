package com.sprint.deokhugam.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.deokhugam.config.TestQuerydslConfig;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.notification.entity.Notification;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.config.JpaAuditingConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({TestQuerydslConfig.class, JpaAuditingConfig.class})
class NotificationRepositoryImplTest {

    @Autowired
    EntityManager em;

    @Autowired
    NotificationRepositoryImpl notificationRepository;

    @Test
    @DisplayName("NotificationRepositoryImpl: 알림 전체 읽음 처리")
    void markAllAsReadByUserId_test() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .nickname("tester")
                .password("1234asdf!")
                .build();
        em.persist(user);

        Book book = Book.builder()
                .title("책 제목")
                .author("작가 이름")
                .isbn("978" + String.format("%010d", new Random().nextInt(1_000_000_000)))
                .publisher("출판사")
                .publishedDate(LocalDate.now())
                .description("설명")
                .thumbnailUrl("https://example.com/thumbnail.jpg")
                .reviewCount(0L)
                .rating(0.0)
                .isDeleted(false)
                .build();
        em.persist(book);

        Review review = Review.builder()
                .content("리뷰 내용")
                .commentCount(0L)
                .likeCount(0L)
                .rating(4)
                .isDeleted(false)
                .user(user)
                .book(book)
                .build();
        em.persist(review);

        Notification n1 = Notification.builder()
                .user(user)
                .review(review)
                .content("알림1")
                .isConfirmed(false)
                .build();

        Notification n2 = Notification.builder()
                .user(user)
                .review(review)
                .content("알림2")
                .isConfirmed(false)
                .build();

        em.persist(n1);
        em.persist(n2);
        em.flush(); // 이 시점까지 DB 반영
        em.clear(); // 영속성 초기화

        UUID userId = user.getId();

        // when
        notificationRepository.markAllAsReadByUserId(userId);

// ⚠️ 꼭 지켜야 함: 영속성 컨텍스트 초기화
        em.flush();
        em.clear(); // ← 이걸로 1차 캐시 싹 밀어줘야 DB에서 최신 데이터 다시 조회됨

// then
        List<Notification> all = em.createQuery("""
                        select n from Notification n
                        where n.user.id = :userId
                        """, Notification.class)
                .setParameter("userId", userId)
                .getResultList();

// 확인용
        for (Notification n : all) {
            System.out.println("ID: " + n.getId() + ", isConfirmed: " + n.isConfirmed());
        }

        assertThat(all).hasSize(2);
        assertThat(all).allSatisfy(n -> assertThat(n.isConfirmed()).isTrue());
    }
}