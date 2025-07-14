package com.sprint.deokhugam.domain.reviewlike.controller;

import com.sprint.deokhugam.domain.reviewlike.dto.data.ReviewLikeDto;
import com.sprint.deokhugam.domain.reviewlike.service.ReviewLikeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reviews/{reviewId}/like")
public class ReviewLikeController {

    private final ReviewLikeService reviewLikeService;

    @PostMapping
    public ResponseEntity<ReviewLikeDto> toggleLike(
        @PathVariable UUID reviewId,
        @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    ) {
        ReviewLikeDto result = reviewLikeService.toggleLike(reviewId, userId);
        return ResponseEntity.ok(result);
    }
}
