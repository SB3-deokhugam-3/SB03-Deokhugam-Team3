package com.sprint.deokhugam.domain.comment.controller;

import com.sprint.deokhugam.domain.comment.controller.api.CommentApi;
import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.deokhugam.domain.comment.service.CommentService;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/comments")
public class CommentController implements CommentApi {

    private final CommentService commentService;

    public ResponseEntity<CommentDto> createComment(CommentCreateRequest request) {
        log.info("[CommentController] 댓글 생성 요청 - reviewId: {}, userId: {}", request.reviewId(),
            request.userId());

        CommentDto commentDto = commentService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(commentDto);
    }

    public ResponseEntity<CommentDto> getComment(UUID commentId) {
        log.info("[CommentController] 댓글 상세 조회 요청 - commentId: {}", commentId);
        CommentDto commentDto = commentService.findById(commentId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(commentDto);
    }

    public ResponseEntity<CommentDto> updateComment(
        UUID commentId, CommentUpdateRequest request, UUID requestUserId) {
        log.info("[CommentController] 댓글 업데이트 요청 - commentId: {}", commentId);
        CommentDto commentDto = commentService.updateById(commentId, request, requestUserId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(commentDto);
    }

    public ResponseEntity<CursorPageResponse<CommentDto>> getCommentsByReviewId(
        UUID reviewId, String cursor, String after, String direction, int limit
    ) {
        CursorPageResponse<CommentDto> response = commentService.findAll(
            reviewId, cursor, after, direction, limit
        );

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    public ResponseEntity<Void> softDeleteComment(
        UUID commentId, UUID requestUserId
    ) {
        commentService.softDelete(commentId, requestUserId);

        return ResponseEntity
            .noContent()
            .build();
    }

    public ResponseEntity<Void> hardDeleteComment(
        UUID commentId, UUID requestUserId
    ) {
        commentService.hardDelete(commentId, requestUserId);

        return ResponseEntity
            .noContent()
            .build();
    }
}
