package com.sprint.deokhugam.domain.popularreview.repository;

import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID>,
    PopularReviewRepositoryCustom {

    Boolean existsByCreatedAtBetween(Instant createdAtAfter, Instant createdAtBefore);
}
