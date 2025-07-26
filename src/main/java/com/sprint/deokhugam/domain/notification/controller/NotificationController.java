package com.sprint.deokhugam.domain.notification.controller;

import com.sprint.deokhugam.domain.notification.controller.api.NotificationApi;
import com.sprint.deokhugam.domain.notification.dto.data.NotificationDto;
import com.sprint.deokhugam.domain.notification.dto.request.NotificationGetRequest;
import com.sprint.deokhugam.domain.notification.service.NotificationService;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    public ResponseEntity<CursorPageResponse<NotificationDto>> getNotifications(
        NotificationGetRequest request
    ) {
        // after가 null이면 현재 시각으로 보정
        if (request.after() == null) {
            request = new NotificationGetRequest(
                request.userId(),
                request.direction(),
                request.cursor(),
                Instant.now(),
                request.limit()
            );
        }

        CursorPageResponse<NotificationDto> response = notificationService.getNotifications(
            request);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> readAllNotifications(UUID requestUserId) {

        notificationService.markAllAsRead(requestUserId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<?> readNotifications(UUID notificationId) {

        notificationService.updateNotification(notificationId);
        return ResponseEntity.noContent().build();
    }

}