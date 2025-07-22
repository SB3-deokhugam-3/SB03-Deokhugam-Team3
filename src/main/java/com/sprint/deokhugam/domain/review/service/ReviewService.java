package com.sprint.deokhugam.domain.review.service;

import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewGetRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.util.List;
import java.util.UUID;

public interface ReviewService {

    ReviewDto create(ReviewCreateRequest request);

    ReviewDto findById(UUID reviewId, UUID requestUserId);

    CursorPageResponse<ReviewDto> findAll(ReviewGetRequest reviewGetRequest, UUID requestUserId);

    void delete(UUID reviewId, UUID userId);

    void hardDelete(UUID reviewId, UUID userId);

    ReviewDto update(UUID reviewId, UUID userId, ReviewUpdateRequest request);

    List<Review> findPopularReviewCandidates();
}
