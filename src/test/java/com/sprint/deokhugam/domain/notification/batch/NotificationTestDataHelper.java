package com.sprint.deokhugam.domain.notification.batch;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.notification.entity.Notification;
import com.sprint.deokhugam.domain.notification.repository.NotificationRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NotificationTestDataHelper {

    @Autowired
    private EntityManager em;
    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void saveTestData() {
        Instant now = Instant.now();

        Book book = Book.builder()
            .title("testbook")
            .author("author")
            .description("desc")
            .publisher("pub")
            .publishedDate(LocalDate.now())
            .isbn("isbn")
            .thumbnailUrl("url")
            .isDeleted(false)
            .rating(0.0)
            .reviewCount(0L)
            .build();
        em.persist(book);

        User user = User.builder()
            .email("testuser@test.com")
            .nickname("testuser")
            .password("password1!")
            .isDeleted(false)
            .build();
        em.persist(user);

        Review review = Review.builder()
            .content("review")
            .rating(1)
            .user(user)
            .book(book)
            .likeCount(0L)
            .commentCount(0L)
            .isDeleted(false)
            .build();
        em.persist(review);

        Notification oldConfirmed = Notification.builder()
            .user(user).review(review)
            .content("old confirmed")
            .isConfirmed(true)
            .build();
        Notification recentConfirmed = Notification.builder()
            .user(user).review(review)
            .content("recent confirmed")
            .isConfirmed(true)
            .build();
        Notification oldUnconfirmed = Notification.builder()
            .user(user).review(review)
            .content("old unconfirmed")
            .isConfirmed(false)
            .build();

        notificationRepository.saveAll(List.of(oldConfirmed, recentConfirmed, oldUnconfirmed));
        em.flush();

        setUpdatedAt(oldConfirmed, now.minus(8, ChronoUnit.DAYS));
        setUpdatedAt(recentConfirmed, now.minus(2, ChronoUnit.DAYS));
        setUpdatedAt(oldUnconfirmed, now.minus(8, ChronoUnit.DAYS));

        notificationRepository.saveAll(List.of(oldConfirmed, recentConfirmed, oldUnconfirmed));
        em.flush();
        em.clear();

        em.createQuery("update Notification n set n.updatedAt = :time where n.id = :id")
            .setParameter("time", now.minus(8, ChronoUnit.DAYS))
            .setParameter("id", oldConfirmed.getId())
            .executeUpdate();

        em.createQuery("update Notification n set n.updatedAt = :time where n.id = :id")
            .setParameter("time", now.minus(2, ChronoUnit.DAYS))
            .setParameter("id", recentConfirmed.getId())
            .executeUpdate();

        em.createQuery("update Notification n set n.updatedAt = :time where n.id = :id")
            .setParameter("time", now.minus(8, ChronoUnit.DAYS))
            .setParameter("id", oldUnconfirmed.getId())
            .executeUpdate();
    }

    private void setUpdatedAt(Notification notification, Instant updatedAt) {
        try {
            Field field = notification.getClass().getSuperclass().getDeclaredField("updatedAt");
            field.setAccessible(true);
            field.set(notification, updatedAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void clearTestData() {
        em.createQuery("DELETE FROM Notification").executeUpdate();
        em.createQuery("DELETE FROM Review").executeUpdate();
        em.createQuery("DELETE FROM Book").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.flush();
        em.clear();
    }
}