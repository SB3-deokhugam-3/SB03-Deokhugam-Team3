package com.sprint.deokhugam.domain.notification.service;

import com.sprint.deokhugam.domain.notification.dto.data.NotificationDto;
import com.sprint.deokhugam.domain.notification.dto.request.NotificationGetRequest;
import com.sprint.deokhugam.domain.notification.entity.Notification;
import com.sprint.deokhugam.domain.notification.exception.InvalidNotificationRequestException;
import com.sprint.deokhugam.domain.notification.mapper.NotificationMapper;
import com.sprint.deokhugam.domain.notification.repository.NotificationRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    @Override
    public CursorPageResponse<NotificationDto> getNotifications(NotificationGetRequest request) {
        if (request.limit() < 1 || request.limit() > 100) {
            throw new InvalidNotificationRequestException("limit", "limit 값은 1 이상 100 이하만 가능합니다.");
        }
        CursorPageResponse<Notification> notifications = notificationRepository.findByUserIdWithCursor(
                request.userId(),
                request.after(),
                request.cursor(),
                request.limit()
        );

        List<NotificationDto> content = notifications.content().stream()
                .map(notificationMapper::toDto)
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
}