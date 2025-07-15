package com.sprint.deokhugam.domain.review.exception;

import com.sprint.deokhugam.global.exception.ForbiddenException;
import java.util.Map;
import java.util.UUID;

public class ReviewUnauthorizedAccessException extends ForbiddenException {

    public ReviewUnauthorizedAccessException(UUID reviewId, UUID userId) {
        super("Review", Map.of("reviewId", reviewId, "userId", userId));
    }
}
