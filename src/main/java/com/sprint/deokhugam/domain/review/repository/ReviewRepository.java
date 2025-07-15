package com.sprint.deokhugam.domain.review.repository;

import com.sprint.deokhugam.domain.review.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, CustomReviewRepository {

    boolean existsByBookIdAndUserId(UUID bookId, UUID userId);

    /**
     * Retrieves a deleted review by its unique identifier.
     *
     * @param reviewId the unique identifier of the review
     * @return an Optional containing the deleted Review if found, or empty if not found
     */
    @Query(value = "SELECT * FROM reviews WHERE id = :id AND is_deleted = true", nativeQuery = true)
    Optional<Review> findDeletedById(@Param("id") UUID reviewId);

}
