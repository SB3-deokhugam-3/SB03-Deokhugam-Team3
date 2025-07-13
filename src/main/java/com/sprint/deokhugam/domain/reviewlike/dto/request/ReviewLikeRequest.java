package com.sprint.deokhugam.domain.reviewlike.dto.request;

import java.util.UUID;

public record ReviewLikeRequest(
    UUID userId,
    boolean liked
) {

}
