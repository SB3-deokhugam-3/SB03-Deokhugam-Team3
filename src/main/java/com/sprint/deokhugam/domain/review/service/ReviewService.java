package com.sprint.deokhugam.domain.review.service;

import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import java.util.UUID;

public interface ReviewService {
    ReviewDto create(ReviewCreateRequest request);

    ReviewDto findById(UUID reviewId);
}
