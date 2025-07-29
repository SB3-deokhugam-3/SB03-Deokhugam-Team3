package com.sprint.deokhugam.domain.reviewlike.controller;

import com.sprint.deokhugam.domain.reviewlike.controller.api.ReviewLikeApi;
import com.sprint.deokhugam.domain.reviewlike.dto.data.ReviewLikeDto;
import com.sprint.deokhugam.domain.reviewlike.service.ReviewLikeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reviews/{reviewId}/like")
public class ReviewLikeController implements ReviewLikeApi {

    private final ReviewLikeService reviewLikeService;

    public ResponseEntity<ReviewLikeDto> toggleLike(
        UUID reviewId, UUID userId
    ) {
        ReviewLikeDto result = reviewLikeService.toggleLike(reviewId, userId);
        return ResponseEntity.ok(result);
    }
}
