package com.sprint.deokhugam.domain.comment.service;

import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import java.util.UUID;

public interface CommentService {

    CommentDto create(CommentCreateRequest request);

    CommentDto findById(UUID commentId);

    CommentDto updateById(UUID commentId, CommentUpdateRequest request, UUID requestUserId);
}
