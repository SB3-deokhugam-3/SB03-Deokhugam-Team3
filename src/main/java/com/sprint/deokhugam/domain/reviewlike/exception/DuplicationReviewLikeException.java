package com.sprint.deokhugam.domain.reviewlike.exception;

import com.sprint.deokhugam.global.exception.ConflictException;
import java.util.Map;
import java.util.UUID;

public class DuplicationReviewLikeException extends ConflictException {

    public DuplicationReviewLikeException(UUID reviewId, UUID userId) {
        super("ReviewLike", Map.of("reviewId", reviewId, "userId", userId));
    }
}
