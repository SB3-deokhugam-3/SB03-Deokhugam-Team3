package com.sprint.deokhugam.domain.notification.service;

import com.sprint.deokhugam.domain.notification.dto.data.NotificationDto;
import com.sprint.deokhugam.domain.notification.dto.request.NotificationGetRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.util.UUID;

public interface NotificationService {

    CursorPageResponse<NotificationDto> getNotifications(NotificationGetRequest requestDto);

    void markAllAsRead(UUID userId);

    NotificationDto create(User user, Review review, String content, boolean isConfirmed);

    void updateNotification(UUID id);
}