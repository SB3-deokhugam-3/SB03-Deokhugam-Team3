package com.sprint.deokhugam.domain.comment.service;

import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    CommentDto create(CommentCreateRequest request);

    CursorPageResponse<CommentDto> findAll(UUID reviewId, String cursor, String direction, int limit);
}
