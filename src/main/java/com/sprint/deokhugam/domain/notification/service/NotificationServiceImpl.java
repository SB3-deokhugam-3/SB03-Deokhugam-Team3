package com.sprint.deokhugam.domain.notification.service;

import com.sprint.deokhugam.domain.notification.dto.data.NotificationDto;
import com.sprint.deokhugam.domain.notification.dto.request.NotificationGetRequest;
import com.sprint.deokhugam.domain.notification.entity.Notification;
import com.sprint.deokhugam.domain.notification.repository.NotificationRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    @Override
    public CursorPageResponse<NotificationDto> getNotifications(NotificationGetRequest request) {
        CursorPageResponse<Notification> notifications = notificationRepository.findByUserIdWithCursor(
                request.userId(),
                request.after(),
                request.cursor(),
                request.limit()
        );

        //1차 캐시상태 직접 반영
        List<NotificationDto> content = notifications.content().stream()
                .map(notification -> NotificationDto.builder()
                        .id(notification.getId())
                        .userId(notification.getUser().getId())
                        .reviewId(notification.getReview().getId())
                        .content(notification.getContent())
                        .isConfirmed(notification.isConfirmed())
                        .createdAt(notification.getCreatedAt())
                        .updatedAt(notification.getUpdatedAt())
                        .build())
                .toList();
        return new CursorPageResponse<>(
                content,
                notifications.nextCursor(),
                notifications.nextAfter(),
                notifications.size(),
                null,
                notifications.hasNext()
        );
    }

    @Transactional
    @Override
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}