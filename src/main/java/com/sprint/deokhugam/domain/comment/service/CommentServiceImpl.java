package com.sprint.deokhugam.domain.comment.service;

import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.deokhugam.domain.comment.entity.Comment;
import com.sprint.deokhugam.domain.comment.exception.CommentNotFoundException;
import com.sprint.deokhugam.domain.comment.exception.CommentUnauthorizedAccessException;
import com.sprint.deokhugam.domain.comment.mapper.CommentMapper;
import com.sprint.deokhugam.domain.comment.repository.CommentRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.exception.UserNotFoundException;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Transactional
    @Override
    public CommentDto create(CommentCreateRequest request) {
        UUID reviewId = request.reviewId();
        UUID userId = request.userId();
        String content = request.content();
        log.info("[comment] 생성 요청 - reviewId: {}, userId: {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> {
            log.warn("[comment] 생성 실패 - 해당하는 리뷰없음 - reviewId: {}, userId: {}", reviewId, userId);
            return new ReviewNotFoundException(reviewId);
        });

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("[comment] 생성 실패 - 해당하는 유저없음 - reviewId: {}, userId: {}", reviewId, userId);
            return new UserNotFoundException(userId, null);
        });

        Comment comment = new Comment(review, user, content);
        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDto(savedComment);

    }

    @Override
    public CommentDto findById(UUID commentId) {
        log.info("[comment] 댓글 상세 조회 요청 - commentId: {}", commentId);

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> {
            log.warn("[comment] 댓글 상세 조회 요청 실패(존재하지않음) - commentId: {}", commentId);
            return new CommentNotFoundException(commentId);
        });

        return commentMapper.toDto(comment);
    }

    @Transactional
    @Override
    public CommentDto updateById(UUID commentId, CommentUpdateRequest request, UUID requestUserId) {
        log.info("[comment] 댓글 업데이트 요청 - commentId: {}", commentId);
        String content = request.content();

        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> {
            log.warn("[comment] 댓글 조회 요청 실패(존재하지않음) - commentId: {}", commentId);
            return new CommentNotFoundException(commentId);
        });

        validateAuthorizedUser(comment, requestUserId);

        comment.update(content);

        return commentMapper.toDto(comment);
    }

    private void validateAuthorizedUser(Comment comment, UUID userId) {
        if (!comment.getUser().getId().equals(userId)) {
            log.warn("[comment] {} - 해당 유저는 권한이 없음: commentId={}, userId={}", comment.getId(),
                userId,
                userId);
            throw new CommentUnauthorizedAccessException(comment.getId(), userId);
        }
    }
}
