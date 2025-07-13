package com.sprint.deokhugam.domain.review.repository;

import com.sprint.deokhugam.domain.review.dto.request.ReviewRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomReviewRepository {

    List<Review> findAll(ReviewRequest params);

    Long countAllByFilterCondition(ReviewRequest params);
}
