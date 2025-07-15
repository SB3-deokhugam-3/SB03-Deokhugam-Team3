package com.sprint.deokhugam.domain.comment.dto.data;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CommentDto(
    UUID id,
    UUID reviewId,
    UUID userId,
    String userNickname,
    String content,
    Instant createdAt,
    Instant updatedAt

) {

}
