package com.sprint.deokhugam.domain.review.exception;

import com.sprint.deokhugam.global.exception.NotFoundException;
import java.util.Map;
import java.util.UUID;

public class ReviewNotSoftDeletedException extends NotFoundException {

    public ReviewNotSoftDeletedException(UUID reviewId) {
        super("review", Map.of("reviewId", reviewId));
    }
}
