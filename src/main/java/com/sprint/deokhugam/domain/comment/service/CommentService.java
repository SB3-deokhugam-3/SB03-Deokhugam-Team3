package com.sprint.deokhugam.domain.comment.service;

import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;

public interface CommentService {

    /**
 * Creates a new comment based on the provided request data.
 *
 * @param request the data required to create a comment
 * @return the created comment as a CommentDto
 */
CommentDto create(CommentCreateRequest request);
}
