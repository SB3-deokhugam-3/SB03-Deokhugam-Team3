package com.sprint.deokhugam.domain.reviewlike.repository;

import com.sprint.deokhugam.domain.reviewlike.entity.ReviewLike;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID>,
    ReviewLikeRepositoryCustom {

    boolean existsByReviewIdAndUserId(UUID reviewId, UUID userId);

    void deleteByReviewIdAndUserId(UUID reviewId, UUID userId);

}
