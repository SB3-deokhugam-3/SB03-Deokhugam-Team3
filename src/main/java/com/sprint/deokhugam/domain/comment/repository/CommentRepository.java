package com.sprint.deokhugam.domain.comment.repository;

import com.sprint.deokhugam.domain.comment.entity.Comment;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @EntityGraph(attributePaths = {"user"})
    Slice<Comment> findByReviewId(UUID reviewId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Slice<Comment> findByReviewIdAndCreatedAtLessThan(UUID reviewId, Instant cursor, Pageable pageable);

    Long countByReviewId(UUID reviewId);
}
