package com.sprint.deokhugam.domain.comment.service;

import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.deokhugam.domain.comment.entity.Comment;
import com.sprint.deokhugam.domain.comment.exception.CommentNotFoundException;
import com.sprint.deokhugam.domain.comment.exception.CommentNotSoftDeletedException;
import com.sprint.deokhugam.domain.comment.exception.CommentUnauthorizedAccessException;
import com.sprint.deokhugam.domain.comment.exception.InvalidCursorTypeException;
import com.sprint.deokhugam.domain.comment.mapper.CommentMapper;
import com.sprint.deokhugam.domain.comment.repository.CommentRepository;
import com.sprint.deokhugam.domain.notification.service.NotificationService;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.exception.UserNotFoundException;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;

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

        review.increaseCommentCount();
        notificationService.create(user, review,
            user.getNickname() + "님이 나의 리뷰 댓글을 남겼습니다.\n" + content, false);

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

    @Override
    public CursorPageResponse<CommentDto> findAll(UUID reviewId, String cursor, String after,
        String direction, int limit) {
        log.info(
            "[CommentService] 댓글 목록 조회 시작 - reviewId={}, cursor={}, after={}, direction={}, limit={}",
            reviewId, cursor, after, direction, limit);

        if (!reviewRepository.existsById(reviewId)) {
            throw new ReviewNotFoundException(reviewId);
        }

        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());

        Instant cursorTime = parseInstant(cursor);
        Instant afterTime = parseInstant(after);

        int fetchSize = limit + 1;
        List<Comment> comments = commentRepository.fetchComments(reviewId, cursorTime, afterTime,
            sortDirection, fetchSize);

        boolean hasNext = comments.size() > limit;
        if (hasNext) {
            comments = comments.subList(0, limit);
        }

        List<CommentDto> dtos = comments.stream()
            .map(commentMapper::toDto)
            .toList();

        Instant nextCursor = hasNext && !comments.isEmpty()
            ? comments.get(comments.size() - 1).getCreatedAt()
            : null;

        CursorPageResponse<CommentDto> response = new CursorPageResponse<>(
            dtos,
            nextCursor != null ? nextCursor.toString() : null,
            nextCursor != null ? nextCursor.toString() : null,
            dtos.size(),
            commentRepository.countByReviewId(reviewId),
            hasNext
        );

        log.info("[CommentService] 댓글 목록 조회 완료 - 결과 수: {}, 다음 페이지 존재: {}", response.size(),
            response.hasNext());
        return response;
    }

    @Transactional
    @Override
    public void softDelete(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> {
                log.warn("[comment] 논리 삭제 실패 - 존재하지 않는 댓글: {}", commentId);
                return new CommentNotFoundException(commentId);
            });

        validateAuthorizedUser(comment, userId);
        comment.softDelete();
        comment.getReview().decreaseCommentCount();
    }

    @Transactional
    @Override
    public void hardDelete(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findByIdIncludingDeleted(commentId)
            .orElseThrow(() -> {
                log.warn("[comment] 물리 삭제 실패 - 존재하지 않는 댓글: {}", commentId);
                return new CommentNotFoundException(commentId);
            });

        if (!comment.getIsDeleted()) {
            log.warn("[comment] 물리 삭제 실패 - 논리 삭제되지 않음: {}", commentId);
            throw new CommentNotSoftDeletedException(commentId);
        }

        validateAuthorizedUser(comment, userId);
        commentRepository.delete(comment);
    }

    private void validateAuthorizedUser(Comment comment, UUID userId) {
        if (!comment.getUser().getId().equals(userId)) {
            log.warn("[comment] 권한 검증 실패 - 해당 유저는 권한이 없음: commentId={}, userId={}", comment.getId(),
                userId);
            throw new CommentUnauthorizedAccessException(comment.getId(), userId);
        }
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new InvalidCursorTypeException(value, e.getMessage());
        }
    }
}
