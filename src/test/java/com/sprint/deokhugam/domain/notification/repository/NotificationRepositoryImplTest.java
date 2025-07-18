package com.sprint.deokhugam.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.deokhugam.config.TestQuerydslConfig;
import com.sprint.deokhugam.domain.notification.entity.Notification;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestQuerydslConfig.class)
class NotificationRepositoryImplTest {

    @Autowired
    EntityManager em;

    @Autowired
    NotificationRepositoryImpl notificationRepository;

    @Test
    void 알림_전체_읽음_처리_테스트() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .nickname("testUser")
                .password("password1!")
                .build();
        em.persist(user);

        Review review = Review.builder()
                .user(user)
                .rating(4)
                .content("good")
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
        em.flush();
        em.clear();

        UUID userId = user.getId();

        // when
        notificationRepository.markAllAsReadByUserId(userId);

        // then
        List<Notification> all = em.createQuery("""
                        select n from Notification n
                        where n.user.id = :userId
                        """, Notification.class)
                .setParameter("userId", userId)
                .getResultList();

        assertThat(all).hasSize(2);
        assertThat(all).allMatch(Notification::isConfirmed);
    }
}