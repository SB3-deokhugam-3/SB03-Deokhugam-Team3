package com.sprint.deokhugam.domain.review.contoller;

import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    /* 리뷰 목록 조회 */
    @GetMapping
    public ResponseEntity<ReviewDto> findAll(ReviewRequest reviewRequest) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(null);
    }


}
