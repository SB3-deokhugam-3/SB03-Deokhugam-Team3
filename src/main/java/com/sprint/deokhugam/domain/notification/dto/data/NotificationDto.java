package com.sprint.deokhugam.domain.notification.dto.data;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NotificationDto(
        UUID id,
        UUID userId,
        UUID reviewId,
        String content,
        boolean confirmed,
        Instant createdAt,
        Instant updatedAt
) {
}