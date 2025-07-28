package com.sprint.deokhugam.domain.user.batch;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.comment.entity.Comment;
import com.sprint.deokhugam.domain.comment.repository.CommentRepository;
import com.sprint.deokhugam.domain.notification.entity.Notification;
import com.sprint.deokhugam.domain.notification.repository.NotificationRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.reviewlike.entity.ReviewLike;
import com.sprint.deokhugam.domain.reviewlike.repository.ReviewLikeRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserCleanupTestDataHelper {
    @Autowired
    private EntityManager em;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Transactional
    public void saveTestData() {
        Instant now = Instant.now();

        // 테스트용 책 생성
        Book book = Book.builder()
            .title("테스트 책")
            .author("테스트 저자")
            .description("테스트 설명")
            .publisher("테스트 출판사")
            .publishedDate(LocalDate.now())
            .isbn("9788965402602")
            .thumbnailUrl("http://test.com/image.jpg")
            .isDeleted(false)
            .rating(4.5)
            .reviewCount(0L)
            .build();
        em.persist(book);

        // 1. 삭제 대상 유저 (isDeleted=true, updatedAt을 과거로 설정)
        User deletedUser = User.builder()
            .email("deleted@test.com")
            .nickname("삭제될유저")
            .password("password1!")
            .isDeleted(true) // 바로 true로 설정
            .build();
        em.persist(deletedUser);

        // 2. 최근 삭제된 유저 (isDeleted=true, 하루 안됨)
        User recentDeletedUser = User.builder()
            .email("recent@test.com")
            .nickname("최근삭제유저")
            .password("password1!")
            .isDeleted(true)
            .build();
        em.persist(recentDeletedUser);

        // 3. 활성 유저 (isDeleted=false)
        User activeUser = User.builder()
            .email("active@test.com")
            .nickname("활성유저")
            .password("password1!")
            .isDeleted(false)
            .build();
        em.persist(activeUser);

        // 각 유저의 리뷰 생성
        Review deletedUserReview = Review.builder()
            .content("삭제될 리뷰")
            .rating(5)
            .user(deletedUser)
            .book(book)
            .likeCount(0L)
            .commentCount(0L)
            .isDeleted(false)
            .build();
        em.persist(deletedUserReview);
        deletedUser.softDelete();

        Review recentDeletedUserReview = Review.builder()
            .content("최근 삭제 유저 리뷰")
            .rating(4)
            .user(recentDeletedUser)
            .book(book)
            .likeCount(0L)
            .commentCount(0L)
            .isDeleted(false)
            .build();
        em.persist(recentDeletedUserReview);
        recentDeletedUser.softDelete();
        em.flush();

        Review activeUserReview = Review.builder()
            .content("활성 유저 리뷰")
            .rating(3)
            .user(activeUser)
            .book(book)
            .likeCount(0L)
            .commentCount(0L)
            .isDeleted(false)
            .build();
        em.persist(activeUserReview);

        // 댓글 생성
        Comment deletedUserComment = Comment.builder()
            .review(deletedUserReview)
            .user(deletedUser)
            .content("삭제될 댓글")
            .isDeleted(false)
            .build();
        em.persist(deletedUserComment);

        Comment activeUserComment = Comment.builder()
            .review(activeUserReview)
            .user(activeUser)
            .content("활성 유저 댓글")
            .isDeleted(false)
            .build();
        em.persist(activeUserComment);

        // 리뷰 좋아요 생성
        ReviewLike deletedUserLike = new ReviewLike(deletedUserReview, deletedUser);
        em.persist(deletedUserLike);

        ReviewLike activeUserLike = new ReviewLike(activeUserReview, activeUser);
        em.persist(activeUserLike);

        // 알림 생성
        Notification deletedUserNotification = Notification.builder()
            .user(deletedUser)
            .review(deletedUserReview)
            .content("삭제될 알림")
            .isConfirmed(false)
            .build();
        em.persist(deletedUserNotification);

        Notification activeUserNotification = Notification.builder()
            .user(activeUser)
            .review(activeUserReview)
            .content("활성 유저 알림")
            .isConfirmed(false)
            .build();
        em.persist(activeUserNotification);

        em.flush();

        // deletedAt 시간을 과거로 설정 (하루 이상 전에 삭제됨) - 네이티브 쿼리 사용
        em.createNativeQuery("UPDATE users SET deleted_at = ? WHERE id = ?")
            .setParameter(1, now.minus(25, ChronoUnit.HOURS))
            .setParameter(2, deletedUser.getId())
            .executeUpdate();

        em.createNativeQuery("UPDATE users SET deleted_at = ? WHERE id = ?")
            .setParameter(1, now.minus(23, ChronoUnit.HOURS)) // 23시간 전
            .setParameter(2, recentDeletedUser.getId())
            .executeUpdate();

        // 책의 리뷰 카운트 업데이트
        em.createQuery("UPDATE Book b SET b.reviewCount = 3 WHERE b.id = :id")
            .setParameter("id", book.getId())
            .executeUpdate();

        em.flush();
        em.clear();
    }

    @Transactional
    public void clearTestData() {
        em.createQuery("DELETE FROM Notification").executeUpdate();
        em.createQuery("DELETE FROM ReviewLike").executeUpdate();
        em.createQuery("DELETE FROM Comment").executeUpdate();
        em.createQuery("DELETE FROM Review").executeUpdate();
        em.createQuery("DELETE FROM Book").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();
        em.flush();
        em.clear();
    }

    @Transactional(readOnly = true)
    public TestDataCounts getDataCounts() {
        // 네이티브 쿼리로 SQLRestriction 우회
        long userCount = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM users")
            .getSingleResult()).longValue();

        long reviewCount = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM reviews")
            .getSingleResult()).longValue();

        long commentCount = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM comments")
            .getSingleResult()).longValue();

        long notificationCount = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM notifications")
            .getSingleResult()).longValue();

        long reviewLikeCount = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM review_likes")
            .getSingleResult()).longValue();

        long bookCount = bookRepository.count(); // Book은 SQLRestriction 없으므로 그대로 사용

        return new TestDataCounts(userCount, reviewCount, commentCount,
            notificationCount, reviewLikeCount, bookCount);
    }

    @Transactional(readOnly = true)
    public UserTypeCounts getUserTypeCounts() {
        // 네이티브 쿼리로 정확한 카운트 조회
        long activeCount = ((Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM users WHERE is_deleted = false")
            .getSingleResult()).longValue();

        long deletedCount = ((Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM users WHERE is_deleted = true")
            .getSingleResult()).longValue();

        return new UserTypeCounts(activeCount, deletedCount);
    }

    // Static 내부 클래스들
    public static class TestDataCounts {
        public final long userCount;
        public final long reviewCount;
        public final long commentCount;
        public final long notificationCount;
        public final long reviewLikeCount;
        public final long bookCount;

        public TestDataCounts(long userCount, long reviewCount, long commentCount,
            long notificationCount, long reviewLikeCount, long bookCount) {
            this.userCount = userCount;
            this.reviewCount = reviewCount;
            this.commentCount = commentCount;
            this.notificationCount = notificationCount;
            this.reviewLikeCount = reviewLikeCount;
            this.bookCount = bookCount;
        }
    }

    public static class UserTypeCounts {
        public final long activeUserCount;
        public final long deletedUserCount;

        public UserTypeCounts(long activeUserCount, long deletedUserCount) {
            this.activeUserCount = activeUserCount;
            this.deletedUserCount = deletedUserCount;
        }
    }
}