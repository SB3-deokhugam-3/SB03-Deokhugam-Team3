package com.sprint.deokhugam.domain.comment.exception;

import com.sprint.deokhugam.global.exception.NotFoundException;
import java.util.Map;
import java.util.UUID;

public class CommentNotSoftDeletedException extends NotFoundException {

    public CommentNotSoftDeletedException(UUID commentId) {
        super("comment", Map.of("commentId", commentId, "message", "논리 삭제되지 않은 댓글입니다."));
    }
}
