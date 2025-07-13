package com.sprint.deokhugam.domain.review.exception;

import com.sprint.deokhugam.global.exception.ConflictException;
import java.util.Map;
import java.util.UUID;

public class DuplicationReviewException extends ConflictException {

    public DuplicationReviewException(UUID bookId, UUID userId) {
        super("Review", Map.of("bookId, userId", bookId, "userId", userId));
    }
}
