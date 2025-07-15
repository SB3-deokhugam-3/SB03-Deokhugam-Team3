package com.sprint.deokhugam.domain.reviewlike.exception;

import com.sprint.deokhugam.global.exception.NotFoundException;
import java.util.Map;
import java.util.UUID;

public class ReviewLikeNotFoundException extends NotFoundException {

    public ReviewLikeNotFoundException(UUID reviewId, UUID userId) {
        super("ReviewLike", Map.of("reviewId", reviewId, "userId", userId));
    }
}
