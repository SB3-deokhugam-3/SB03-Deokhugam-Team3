package com.sprint.deokhugam.domain.comment.exception;

import com.sprint.deokhugam.global.exception.ForbiddenException;
import java.util.Map;
import java.util.UUID;

public class CommentUnauthorizedAccessException extends ForbiddenException {

    public CommentUnauthorizedAccessException(UUID commentId, UUID userId) {
        super("Comment", Map.of("commentId", commentId, "userId", userId));
    }
}
