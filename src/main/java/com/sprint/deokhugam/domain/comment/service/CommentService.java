package com.sprint.deokhugam.domain.comment.service;

import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.util.UUID;

public interface CommentService {

    CommentDto create(CommentCreateRequest request);

    CursorPageResponse<CommentDto> findAll(UUID reviewId, String cursor, String after,
        String direction, int limit);

    CommentDto findById(UUID commentId);

    CommentDto updateById(UUID commentId, CommentUpdateRequest request, UUID requestUserId);

    void softDelete(UUID commentId, UUID userId);

    void hardDelete(UUID commentId, UUID userId);
}
