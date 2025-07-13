package com.sprint.deokhugam.domain.review.exception;

import com.sprint.deokhugam.global.exception.NotFoundException;
import java.util.Map;
import java.util.UUID;

public class ReviewNotFoundException extends NotFoundException {

    public ReviewNotFoundException(UUID reviewId) {
        super("Review", Map.of("reviewId", reviewId));
    }
}
