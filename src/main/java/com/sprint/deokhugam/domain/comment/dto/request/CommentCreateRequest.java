package com.sprint.deokhugam.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommentCreateRequest(
    @NotNull UUID reviewId,
    @NotNull UUID userId,
    @NotBlank String content
) {

}
