package com.sprint.deokhugam.domain.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.notification.dto.data.NotificationDto;
import com.sprint.deokhugam.domain.notification.exception.InvalidNotificationRequestException;
import com.sprint.deokhugam.domain.notification.service.NotificationService;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ActiveProfiles("test")
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 정상_요청_시_알림_목록을_반환한다() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        Instant now = Instant.now();

        NotificationDto dto = NotificationDto.builder()
            .id(notificationId)
            .userId(userId)
            .reviewId(reviewId)
            .content("알림 테스트")
            .confirmed(false)
            .createdAt(now)
            .updatedAt(now)
            .build();

        CursorPageResponse<NotificationDto> response = new CursorPageResponse<>(
            List.of(dto),
            "nextCursor",
            now.toString(),
            1,
            null,
            true
        );

        Mockito.when(notificationService.getNotifications(any()))
            .thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/notifications")
            .param("userId", userId.toString())
            .param("limit", "10")
            .param("direction", "DESC")
        );

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].content").value("알림 테스트"))
            .andExpect(jsonPath("$.nextCursor").value("nextCursor"))
            .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    void limit이_1이하이면_400에러를_반환한다() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        // when
        ResultActions result =
            mockMvc.perform(get("/api/notifications")
                .param("userId", userId.toString())
                .param("limit", "0")
            );

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void limit이_100이상이면_400에러를_반환한다() throws Exception {
        //given
        UUID userId = UUID.randomUUID();

        //when
        ResultActions result = mockMvc.perform(get("/api/notifications")
            .param("userId", userId.toString())
            .param("limit", "101"));

        //then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void 사용자의_모든_알림을_읽음_처리할_수_있다() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        doNothing().when(notificationService).markAllAsRead(userId);

        // when
        ResultActions result = mockMvc.perform(patch("/api/notifications/read-all")
            .header("Deokhugam-Request-User-ID", userId.toString()));
        // then

        result.andExpect(status().isNoContent());
    }

    @Test
    void 알림_ID를_통한_읽음_처리_요청이_성공하면_204를_반환한다() throws Exception {
        // given
        UUID notificationId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();
        doNothing().when(notificationService).updateNotification(notificationId);

        // when
        ResultActions result = mockMvc.perform(
            patch("/api/notifications/{notificationId}", notificationId)
                .header("Deokhugam-Request-User-ID", requestUserId.toString())
        );

        // then
        result.andExpect(status().isNoContent());
        Mockito.verify(notificationService).updateNotification(notificationId);
    }

    @Test
    void 존재하지_않는_알림_ID로_읽음_처리_요청_시_400_에러를_반환한다() throws Exception {
        // given
        UUID notificationId = UUID.randomUUID();
        Mockito.doThrow(new InvalidNotificationRequestException("id", "해당 알림은 존재하지 않습니다."))
            .when(notificationService).updateNotification(notificationId);

        // when
        ResultActions result = mockMvc.perform(
            patch("/api/notifications/{notificationId}", notificationId)
        );

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("NOTIFICATION_INVALID_INPUT_VALUE"));
    }
}