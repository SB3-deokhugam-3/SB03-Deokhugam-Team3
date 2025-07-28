package com.sprint.deokhugam.domain.comment.repository;

import com.sprint.deokhugam.domain.comment.entity.Comment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CustomCommentRepository{

    Long countByReviewId(UUID reviewId);

    @Query(value = "SELECT * FROM comments WHERE id = :id", nativeQuery = true)
    Optional<Comment> findByIdIncludingDeleted(@Param("id") UUID commentId);

    @Modifying
    @Query(value = "DELETE FROM comments WHERE user_id IN (:userIds)", nativeQuery = true)
    @Transactional
    void deleteByUserIdIn(@Param("userIds") List<UUID> userIds);
}
