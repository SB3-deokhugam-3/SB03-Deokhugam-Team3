package com.sprint.deokhugam.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.sprint.deokhugam.domain.notification.dto.data.NotificationDto;
import com.sprint.deokhugam.domain.notification.dto.request.NotificationGetRequest;
import com.sprint.deokhugam.domain.notification.entity.Notification;
import com.sprint.deokhugam.domain.notification.exception.InvalidNotificationRequestException;
import com.sprint.deokhugam.domain.notification.mapper.NotificationMapper;
import com.sprint.deokhugam.domain.notification.repository.NotificationRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void 정상_요청_시_알림_목록을_반환한다() {
        // given
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        Instant now = Instant.now();
        int limit = 3;

        Notification entity = Notification.builder()
                .content("테스트 알림")
                .build();

        NotificationDto dto = NotificationDto.builder()
                .id(notificationId)
                .userId(userId)
                .reviewId(reviewId)
                .content("테스트 알림")
                .isConfirmed(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        CursorPageResponse<Notification> fakeResponse = new CursorPageResponse<>(
                List.of(entity),
                "nextCursor",
                now.toString(),
                1,
                null,
                true
        );

        NotificationGetRequest request = new NotificationGetRequest(
                userId,
                null,
                null,
                now,
                limit
        );

        when(notificationRepository.findByUserIdWithCursor(userId, now, null, limit))
                .thenReturn(fakeResponse);
        when(notificationMapper.toDto(entity)).thenReturn(dto);

        // when
        CursorPageResponse<NotificationDto> response = notificationService.getNotifications(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).content()).isEqualTo("테스트 알림");
        assertThat(response.nextCursor()).isEqualTo("nextCursor");
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void limit이_0이면_예외가_발생한다() {
        // given
        NotificationGetRequest request = new NotificationGetRequest(
                UUID.randomUUID(),
                null,
                null,
                Instant.now(),
                0
        );

        // when & then
        assertThatThrownBy(() -> notificationService.getNotifications(request))
                .isInstanceOf(InvalidNotificationRequestException.class)
                .hasMessage("limit 값은 1 이상 100 이하만 가능합니다.");
    }

    @Test
    void limit이_101이면_에외가_발생한다() {
        // given
        NotificationGetRequest request = new NotificationGetRequest(
                UUID.randomUUID(),
                null,
                null,
                Instant.now(),
                101
        );

        // when & then
        assertThatThrownBy(() -> notificationService.getNotifications(request))
                .isInstanceOf(InvalidNotificationRequestException.class)
                .hasMessage("limit 값은 1 이상 100 이하만 가능합니다.");
    }
}