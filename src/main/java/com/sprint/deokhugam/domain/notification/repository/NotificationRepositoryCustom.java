package com.sprint.deokhugam.domain.notification.repository;

import com.sprint.deokhugam.domain.notification.entity.Notification;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import java.util.UUID;

public interface NotificationRepositoryCustom {
    CursorPageResponse<Notification> findByUserIdWithCursor(
            UUID userId,
            Instant after,
            UUID cursor,
            int limit
    );
}