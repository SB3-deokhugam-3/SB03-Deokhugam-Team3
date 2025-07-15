package com.sprint.deokhugam.domain.reviewlike.dto.data;

import java.util.UUID;

public record ReviewLikeDto(
    UUID reviewId,
    UUID userId,
    boolean liked
) {

}
