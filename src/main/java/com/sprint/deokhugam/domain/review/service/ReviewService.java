package com.sprint.deokhugam.domain.review.service;

import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewGetRequest;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.util.UUID;

public interface ReviewService {

    ReviewDto create(ReviewCreateRequest request);

    ReviewDto findById(UUID reviewId);

    CursorPageResponse<ReviewDto> findAll(ReviewGetRequest reviewGetRequest, UUID requestUserId);

    void delete(UUID reviewId, UUID userId);

    void hardDelete(UUID reviewId, UUID userId);
}
