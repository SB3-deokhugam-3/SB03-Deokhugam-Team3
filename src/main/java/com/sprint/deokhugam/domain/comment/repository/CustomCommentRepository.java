package com.sprint.deokhugam.domain.comment.repository;

import com.sprint.deokhugam.domain.comment.entity.Comment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;

public interface CustomCommentRepository {

     List<Comment> fetchComments(
        UUID reviewId,
        Instant cursor,
        Instant after,
        Sort.Direction direction,
        int fetchSize
    );
}
