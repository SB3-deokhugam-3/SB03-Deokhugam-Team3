package com.sprint.deokhugam.domain.reviewlike.repository;

import com.sprint.deokhugam.domain.reviewlike.entity.ReviewLike;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

    boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);

    void deleteByReviewIdAndUserId(UUID reviewId, UUID userId);

    @Modifying
    @Query(value = "DELETE FROM review_likes WHERE user_id IN (:userIds)", nativeQuery = true)
    @Transactional
    void deleteByUserIdIn(@Param("userIds") List<UUID> userIds);

}
