package com.sprint.deokhugam.domain.review.controller;

import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewGetRequest;
import com.sprint.deokhugam.domain.review.service.ReviewService;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    /* 리뷰 목록 조회 */
    @GetMapping
    public ResponseEntity<CursorPageResponse<ReviewDto>> findAll(
        @Valid ReviewGetRequest reviewGetRequest,
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
        CursorPageResponse<ReviewDto> cursorReviewDtoList = this.reviewService.findAll(
            reviewGetRequest, requestUserId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(cursorReviewDtoList);
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(@RequestBody @Valid ReviewCreateRequest request) {
        ReviewDto reviewDto = reviewService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewDto);
    }

    /* 리뷰 논리 삭제 */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> deleteReview(@PathVariable UUID reviewId,
        @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {
        HttpStatus status = reviewService.delete(reviewId, userId);

        return ResponseEntity
            .status(status)
            .body(null);
    }

    /* 리뷰 물리 삭제 */
    @DeleteMapping("/{reviewId}/hard")
    public ResponseEntity<ReviewDto> hardDeleteReview(@PathVariable UUID reviewId,
        @RequestHeader("Deokhugam-Request-User-ID") UUID userId) {
        HttpStatus status = reviewService.hardDelete(reviewId, userId);

        return ResponseEntity
            .status(status)
            .body(null);
    }

}
