package com.sprint.deokhugam.domain.notification.service;

import com.sprint.deokhugam.domain.notification.dto.data.NotificationDto;
import com.sprint.deokhugam.domain.notification.dto.request.NotificationGetRequest;
import com.sprint.deokhugam.domain.notification.entity.Notification;
import com.sprint.deokhugam.domain.notification.mapper.NotificationMapper;
import com.sprint.deokhugam.domain.notification.repository.NotificationRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.exception.InvalidUserRequestException;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationDto create(User user, Review review, String content, boolean isConfirmed) {
        log.info("[notification] 알림 등록 요청 - user: {}, review: {}", user, review);
        if (user == null || review == null || content == null) {
            log.warn("[notification] 알림 생성 실패 - user: {}, review: {}", user, review);
            throw new InvalidUserRequestException("error", "null 값이 들어왔습니다.");
        }
        Notification notification = new Notification(review.getUser(), review, content,
            isConfirmed);

        Notification saved = notificationRepository.save(notification);
        log.info("[notification] 알림 생성 완료 - user: {}, review: {}", user, review);
        return notificationMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public CursorPageResponse<NotificationDto> getNotifications(NotificationGetRequest request) {
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

    @Transactional
    @Override
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}