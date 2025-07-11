package com.sprint.deokhugam.domain.comment.repository;

import com.sprint.deokhugam.domain.comment.entity.Comment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

}
