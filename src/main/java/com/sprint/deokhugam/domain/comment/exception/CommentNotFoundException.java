package com.sprint.deokhugam.domain.comment.exception;

import com.sprint.deokhugam.global.exception.NotFoundException;
import java.util.Map;
import java.util.UUID;

public class CommentNotFoundException extends NotFoundException {

    public CommentNotFoundException(UUID commentId) {
        super("Comment", Map.of("id", commentId));
    }
}
