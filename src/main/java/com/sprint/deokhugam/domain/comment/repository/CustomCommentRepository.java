package com.sprint.deokhugam.domain.comment.repository;

import com.sprint.deokhugam.domain.comment.entity.Comment;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Sort;

public interface CustomCommentRepository {

    List<Comment> fetchComments(
        UUID reviewId,
        Instant cursor,
        UUID after,
        Sort.Direction direction,
        int fetchSize
    );

    Map<UUID, Long> countByReviewIdBetween(Instant start, Instant end);
}
