package com.sprint.deokhugam.domain.review.repository;

import com.sprint.deokhugam.domain.review.entity.Review;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, CustomReviewRepository {

    boolean existsByBookIdAndUserId(UUID bookId, UUID userId);

}
