package com.sprint.deokhugam.domain.review.controller;

import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    /* 리뷰 목록 조회 */
    @GetMapping
    public ResponseEntity<ReviewDto> findAll(ReviewRequest reviewRequest) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(null);
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(@RequestBody @Valid ReviewCreateRequest request){
        log.info("[BookController] 리뷰 생성 요청 - bookId: {}, userId: {}", request.bookId(), request.userId());
        ReviewDto reviewDto = reviewService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewDto);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> getReview(@PathVariable UUID reviewId){
        log.info("[ReviewController] 리뷰 상세 정보 요청 - id: {}", reviewId);
        ReviewDto reviewDto = reviewService.findById(reviewId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(reviewDto);
    }

}
