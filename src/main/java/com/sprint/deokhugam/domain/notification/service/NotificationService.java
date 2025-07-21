package com.sprint.deokhugam.domain.notification.service;

import com.sprint.deokhugam.domain.notification.dto.data.NotificationDto;
import com.sprint.deokhugam.domain.notification.dto.request.NotificationGetRequest;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.util.UUID;

public interface NotificationService {

    CursorPageResponse<NotificationDto> getNotifications(NotificationGetRequest requestDto);

    void markAllAsRead(UUID userId);

}