package com.sprint.deokhugam.domain.reviewlike.controller.api;

import com.sprint.deokhugam.domain.reviewlike.dto.data.ReviewLikeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "리뷰 관리")
public interface ReviewLikeApi {

    @PostMapping
    @Operation(
        summary = "리뷰 좋아요",
        description = "리뷰에 좋아요를 추가하거나 취소합니다.",
        operationId = "likeReview",
        security = @SecurityRequirement(name = "CustomHeaderAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "리뷰 좋아요 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ReviewLikeDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404", description = "리뷰 정보 없음",
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
    ResponseEntity<ReviewLikeDto> toggleLike(
        @PathVariable @Parameter(description = "리뷰 ID", required = true) UUID reviewId,
        @RequestHeader("Deokhugam-Request-User-ID")
        @Parameter(description = "요청자 ID", required = true) UUID userId
    );

}
