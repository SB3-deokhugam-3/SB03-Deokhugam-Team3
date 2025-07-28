package com.sprint.deokhugam.domain.review.repository;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.review.entity.Review;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, CustomReviewRepository {

    boolean existsByBookIdAndUserId(UUID bookId, UUID userId);

    @Query(value = "SELECT * FROM reviews WHERE id = :id AND is_deleted = true", nativeQuery = true)
    Optional<Review> findDeletedById(@Param("id") UUID reviewId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book = :book")
    Double findAverageRatingByBook(@Param("book") Book book);

    /* 배치에서 사용 */
    @Query(value = "SELECT * FROM reviews WHERE (comment_count*0.7 + like_count*0.3) > 0 AND is_deleted = false ORDER BY (comment_count*0.7 + like_count*0.3) DESC ", nativeQuery = true)
    List<Review> findAllByCommentCountAndLikeCountWithSorting();

    @Modifying
    @Query(value = "DELETE FROM reviews WHERE user_id IN (:userIds)", nativeQuery = true)
    void deleteAllByUserIdIn(@Param("userIds") List<UUID> userIds);

    @Query(value = "SELECT DISTINCT book_id FROM reviews WHERE user_id IN (:userIds)", nativeQuery = true)
    List<UUID> findBookIdsByUserIdIn(@Param("userIds") List<UUID> userIds);

    @Modifying
    @Query(value = """
    UPDATE reviews 
    SET comment_count = (
        SELECT COUNT(*) 
        FROM comments c 
        WHERE c.review_id = reviews.id 
        AND c.is_deleted = false
    )
    WHERE is_deleted = false
    """, nativeQuery = true)
    void recalculateReviewCommentCounts();
}
