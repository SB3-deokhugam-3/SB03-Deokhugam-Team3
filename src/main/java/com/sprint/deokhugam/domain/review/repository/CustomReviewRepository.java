package com.sprint.deokhugam.domain.review.repository;

import com.sprint.deokhugam.domain.review.dto.request.ReviewGetRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomReviewRepository {

    List<Review> findAll(ReviewGetRequest params);

    Long countAllByFilterCondition(ReviewGetRequest params);
}
