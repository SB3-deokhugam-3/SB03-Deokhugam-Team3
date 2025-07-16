package com.sprint.deokhugam.domain.comment.controller;

import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.service.CommentService;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    ResponseEntity<CursorPageResponse<CommentDto>> getCommentsByReviewId(
        @RequestParam @NotNull UUID reviewId,
        @RequestParam(required = false, defaultValue = "DESC") String direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false, defaultValue = "50") int limit
    ) {
        CursorPageResponse<CommentDto> response = commentService.findAll(
            reviewId, cursor, direction, limit
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
