package com.sprint.deokhugam.domain.notification.controller.api;

import com.sprint.deokhugam.domain.notification.dto.data.NotificationDto;
import com.sprint.deokhugam.domain.notification.dto.request.NotificationGetRequest;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "알림 관리")
public interface NotificationApi {

    @PatchMapping("/{notificationId}")
    @Operation(
        summary = "알림 읽음 상태 업데이트",
        description = "특정 알림의 읽음 상태를 업데이트합니다.",
        operationId = "updateNotification"
        , security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "403", description = "알림 수정 권한 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = NotificationDto.class))
        ),
        @ApiResponse(
            responseCode = "200", description = "알림 상태 업데이트 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404", description = "알림 정보 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<?> readNotifications(
        @PathVariable @Parameter(description = "알림 ID", required = true) UUID notificationId
    );

    @PatchMapping("/read-all")
    @Operation(
        summary = "모든 알림 읽음 처리",
        description = "사용자의 모든 알림을 읽음 상태로 처리합니다.",
        operationId = "markAllAsRead"
        , security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "알림 읽음 처리 성공",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (사용자 ID 누락)",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "사용자 정보 없음",
            content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<?> readAllNotifications(
        @Parameter(description = "요청자 ID", required = true)
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    );

    @GetMapping
    @Operation(
        summary = "알림 목록 조회",
        description = "사용자의 알림 목록을 조회합니다.",
        operationId = "getNotifications"
        , security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "알림 목록 조회 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = NotificationDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (정렬 방향 오류, 페이지네이션 파라미터 오류, 사용자 ID 누락)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404", description = "사용자 정보 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500", description = "서버 내부 오류",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<CursorPageResponse<NotificationDto>> getNotifications(
        @Parameter(
            description = """
                사용자 알림 목록 조회용 DTO<br>
                - **userId**: 사용자 ID<br>
                - **direction**: 정렬 방향 (ASC, DESC, 기본값: DESC)<br>
                - **cursor**: 커서 페이지네이션을 커서<br>
                - **after**: createdAt 기준 보조 커서<br>
                - **limit**: 페이지 크기 (기본값: 20)
                """
        )
        @Valid @ModelAttribute NotificationGetRequest request
    );
}