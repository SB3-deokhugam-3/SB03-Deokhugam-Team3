package com.sprint.deokhugam.domain.comment.controller.api;

import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "댓글 관리")
public interface CommentApi {

    @GetMapping
    @Operation(
        summary = "리뷰 댓글 목록 조회",
        description = "특정 리뷰에 달린 댓글 목록을 시간순으로 조회합니다.",
        operationId = "getCommentsByReviewId"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "댓글 목록 조회 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = CommentDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (정렬 방향 오류, 페이지네이션 파라미터 오류, 리뷰 ID 누락)",
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
    ResponseEntity<CursorPageResponse<CommentDto>> getCommentsByReviewId(
        @RequestParam @Parameter(description = "리뷰 ID", required = true) UUID reviewId,
        @RequestParam(required = false)
        @Parameter(description = "커서 페이지네이션 커서") String cursor,
        @RequestParam(required = false)
        @Parameter(description = "보조 커서(createdAt)") String after,
        @RequestParam(required = false, defaultValue = "DESC")
        @Parameter(description = "정렬 방향") String direction,
        @RequestParam(required = false, defaultValue = "50")
        @Parameter(description = "페이지 크기") int limit
    );

    @PostMapping
    @Operation(
        summary = "댓글 등록",
        description = "새로운 댓글을 등록합니다.",
        operationId = "createComment"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "댓글 등록 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = CommentDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)",
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
    ResponseEntity<CommentDto> createComment(
        @RequestBody @Valid CommentCreateRequest request
    );

    @GetMapping("/{commentId}")
    @Operation(
        summary = "댓글 상세 정보 조회",
        description = "특정 댓글의 상세 정보를 조회합니다.",
        operationId = "getComment"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "댓글 조회 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = CommentDto.class))
        ),
        @ApiResponse(
            responseCode = "404", description = "댓글 정보 없음",
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
    ResponseEntity<CommentDto> getComment(
        @PathVariable @Parameter(description = "댓글 ID", required = true) UUID commentId
    );

    @DeleteMapping("/{commentId}")
    @Operation(
        summary = "댓글 논리 삭제",
        description = "본인이 작성한 댓글을 논리적으로 삭제합니다.",
        operationId = "deleteComment"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)"),
        @ApiResponse(responseCode = "404", description = "댓글 정보 없음"),
        @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> softDeleteComment(
        @PathVariable @Parameter(description = "댓글 ID", required = true) UUID commentId,
        @RequestHeader("Deokhugam-Request-User-ID")
        @Parameter(description = "요청자 ID", required = true) UUID requestUserId
    );

    @DeleteMapping("/{commentId}/hard")
    @Operation(
        summary = "댓글 물리 삭제",
        description = "본인이 작성한 댓글을 물리적으로 삭제합니다.",
        operationId = "permanentDeleteComment"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (요청자 ID 누락)"),
        @ApiResponse(responseCode = "404", description = "댓글 정보 없음"),
        @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> hardDeleteComment(
        @PathVariable @Parameter(description = "댓글 ID", required = true) UUID commentId,
        @RequestHeader("Deokhugam-Request-User-ID")
        @Parameter(description = "요청자 ID", required = true) UUID requestUserId
    );

    @PatchMapping("/{commentId}")
    @Operation(
        summary = "댓글 수정",
        description = "본인이 작성한 댓글을 수정합니다.",
        operationId = "updateComment"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "댓글 수정 성공",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = CommentDto.class))
        ),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404", description = "댓글 정보 없음",
            content = @Content(
                mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403", description = "댓글 수정 권한 없음",
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
    ResponseEntity<CommentDto> updateComment(
        @Parameter(description = "댓글 ID", required = true) UUID commentId,
        @RequestBody @Valid CommentUpdateRequest request,
        @Parameter(description = "요청자 ID", required = true)
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    );

}
