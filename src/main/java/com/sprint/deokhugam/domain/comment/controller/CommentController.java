package com.sprint.deokhugam.domain.comment.controller;

import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.deokhugam.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    ResponseEntity<CommentDto> createComment(@RequestBody @Valid CommentCreateRequest request) {
        log.info("[CommentController] 댓글 생성 요청 - reviewId: {}, userId: {}", request.reviewId(),
            request.userId());

        CommentDto commentDto = commentService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(commentDto);

    }

    @GetMapping("/{commentId}")
    ResponseEntity<CommentDto> getComment(@PathVariable UUID commentId) {
        log.info("[CommentController] 댓글 상세 조회 요청 - commentId: {}", commentId);
        CommentDto commentDto = commentService.findById(commentId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(commentDto);
    }

    @PatchMapping("/{commentId}")
    ResponseEntity<CommentDto> updateComment(@PathVariable UUID commentId,
        @RequestBody @Valid CommentUpdateRequest request,
        @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId) {
        log.info("[CommentController] 댓글 업데이트 요청 - commentId: {}", commentId);
        CommentDto commentDto = commentService.updateById(commentId, request, requestUserId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(commentDto);
    }


}
