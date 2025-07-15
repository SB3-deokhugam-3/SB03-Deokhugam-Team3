package com.sprint.deokhugam.domain.comment.controller;

import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    /**
     * Handles HTTP POST requests to create a new comment.
     *
     * Accepts a validated comment creation request and returns the created comment data with HTTP status 201 (Created).
     *
     * @param request the validated request body containing comment creation details
     * @return a ResponseEntity containing the created CommentDto and HTTP status 201
     */
    @PostMapping
    ResponseEntity<CommentDto> createComment(@RequestBody @Valid CommentCreateRequest request) {
        log.info("[CommentController] 댓글 생성 요청 - reviewId: {}, userId: {}", request.reviewId(),
            request.userId());

        CommentDto commentDto = commentService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(commentDto);

    }


}
