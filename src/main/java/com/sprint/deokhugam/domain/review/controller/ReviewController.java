package com.sprint.deokhugam.domain.review.controller;

import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.popularreview.service.PopularReviewService;
import com.sprint.deokhugam.domain.review.controller.api.ReviewApi;
import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewGetRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sprint.deokhugam.domain.review.service.ReviewService;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/reviews")
public class ReviewController implements ReviewApi {

    private final ReviewService reviewService;
    private final PopularReviewService popularReviewService;

    public ResponseEntity<CursorPageResponse<ReviewDto>> findAll(
        ReviewGetRequest reviewGetRequest, UUID requestUserId) {
        log.info("[ReviewController] 리뷰 조회 : reviewGetRequest: {}, requestUserId:{}",
            reviewGetRequest.toString(), requestUserId);
        CursorPageResponse<ReviewDto> cursorReviewDtoList = this.reviewService.findAll(
            reviewGetRequest, requestUserId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(cursorReviewDtoList);
    }

    public ResponseEntity<ReviewDto> createReview(ReviewCreateRequest request) {
        log.info("[ReviewController] 리뷰 생성 요청 - bookId: {}, userId: {}", request.bookId(),
            request.userId());
        ReviewDto reviewDto = reviewService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(reviewDto);
    }

    public ResponseEntity<ReviewDto> getReview(
        UUID reviewId, UUID requestUserId
    ) {
        log.info("[ReviewController] 리뷰 상세 정보 요청 - id: {}", reviewId);
        ReviewDto reviewDto = reviewService.findById(reviewId, requestUserId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(reviewDto);
    }

    /* 리뷰 논리 삭제 */
    public ResponseEntity<Void> deleteReview(
        UUID reviewId, UUID requestUserId) {
        reviewService.delete(reviewId, requestUserId);

        return ResponseEntity
            .noContent()
            .build();
    }

    /* 리뷰 물리 삭제 */
    public ResponseEntity<Void> hardDeleteReview(UUID reviewId, UUID requestUserId) {
        reviewService.hardDelete(reviewId, requestUserId);

        return ResponseEntity
            .noContent()
            .build();
    }

    public ResponseEntity<ReviewDto> updateReview(
        UUID reviewId, UUID requestUserId, ReviewUpdateRequest request
    ) {
        ReviewDto reviewDto = reviewService.update(reviewId, requestUserId, request);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(reviewDto);
    }

    public ResponseEntity<CursorPageResponse<PopularReviewDto>> getPopularReviews(
        PeriodType period, String direction, String cursor, Instant after, int limit
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);

        CursorPageResponse<PopularReviewDto> response = popularReviewService.getPopularReviews(
            period, sortDirection, cursor, after, limit
        );

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

}
