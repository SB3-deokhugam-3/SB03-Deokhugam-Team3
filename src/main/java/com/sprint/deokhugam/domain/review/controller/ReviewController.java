package com.sprint.deokhugam.domain.review.controller;

import com.sprint.deokhugam.domain.popularreview.PeriodType;
import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.popularreview.service.PopularReviewService;
import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewGetRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sprint.deokhugam.domain.review.service.ReviewService;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final PopularReviewService popularReviewService;

    /* 리뷰 목록 조회 */
    @GetMapping
    public ResponseEntity<CursorPageResponse<ReviewDto>> findAll(
        @Valid ReviewGetRequest reviewGetRequest,
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
        log.info("[ReviewController] 리뷰 조회 : reviewGetRequest: {}, requestUserId:{}",
            reviewGetRequest.toString(), requestUserId);
        CursorPageResponse<ReviewDto> cursorReviewDtoList = this.reviewService.findAll(
            reviewGetRequest, requestUserId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(cursorReviewDtoList);
    }

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(@RequestBody @Valid ReviewCreateRequest request) {
        log.info("[ReviewController] 리뷰 생성 요청 - bookId: {}, userId: {}", request.bookId(),
            request.userId());
        ReviewDto reviewDto = reviewService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(reviewDto);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> getReview(
        @PathVariable UUID reviewId,
        @RequestHeader("Deokhugam-Request-User-ID") @NotNull UUID requestUserId
    ) {
        log.info("[ReviewController] 리뷰 상세 정보 요청 - id: {}", reviewId);
        ReviewDto reviewDto = reviewService.findById(reviewId, requestUserId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(reviewDto);
    }

    /* 리뷰 논리 삭제 */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable @NotNull UUID reviewId,
        @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {
        reviewService.delete(reviewId, userId);

        return ResponseEntity
            .noContent()
            .build();
    }

    /* 리뷰 물리 삭제 */
    @DeleteMapping("/{reviewId}/hard")
    public ResponseEntity<Void> hardDeleteReview(@PathVariable @NotNull UUID reviewId,
        @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {
        reviewService.hardDelete(reviewId, userId);

        return ResponseEntity
            .noContent()
            .build();

    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(
        @PathVariable UUID reviewId,
        @RequestHeader("Deokhugam-Request-User-ID") @NotNull UUID userId,
        @Valid @RequestBody ReviewUpdateRequest request
    ) {
        ReviewDto reviewDto = reviewService.update(reviewId, userId, request);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(reviewDto);
    }

    @GetMapping("/popular")
    public ResponseEntity<CursorPageResponse<PopularReviewDto>> getPopularReviews(
        @RequestParam(defaultValue = "DAILY") PeriodType period,
        @RequestParam(defaultValue = "ASC") String direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Instant after,
        @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit
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
