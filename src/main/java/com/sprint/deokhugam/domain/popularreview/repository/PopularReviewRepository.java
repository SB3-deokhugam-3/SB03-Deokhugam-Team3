package com.sprint.deokhugam.domain.popularreview.repository;

import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID> {

    @Query(value = "SELECT EXISTS(Select 1 FROM popular_review_rankings WHERE created_at BETWEEN :start AND :end)", nativeQuery = true)
    Boolean existsByCreatedAtBetween(@Param("start") Instant createdAtAfter,
        @Param("end") Instant createdAtBefore);

}
