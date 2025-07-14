package com.sprint.deokhugam.domain.review.service;

import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public interface ReviewService {

    ReviewDto create(ReviewCreateRequest request);

    HttpStatus delete(UUID reviewId, UUID userId);

    HttpStatus hardDelete(UUID reviewId, UUID userId);
}
