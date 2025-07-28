package com.sprint.deokhugam.domain.poweruser.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PowerUserDto(
    UUID userId,
    String nickname,
    String period,
    Instant createdAt,
    Long rank,
    Double score,
    Double reviewScoreSum,
    Long likeCount,
    Long commentCount
) {

}
