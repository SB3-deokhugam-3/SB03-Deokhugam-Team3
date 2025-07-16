package com.sprint.deokhugam.domain.comment.service;

import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.entity.Comment;
import com.sprint.deokhugam.domain.comment.exception.InvalidCursorTypeException;
import com.sprint.deokhugam.domain.comment.mapper.CommentMapper;
import com.sprint.deokhugam.domain.comment.repository.CommentRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.exception.UserNotFoundException;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.exception.InvalidTypeException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

        return commentMapper.toDto(savedComment);

    }

    @Override
    public CursorPageResponse<CommentDto> findAll(UUID reviewId, String cursor, String direction, int limit) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ReviewNotFoundException(reviewId);
        }

        // cursor String → Instant 변환
        Instant createdAt = null;
        if (cursor != null) {
            try {
                createdAt = Instant.parse(cursor);
            } catch (DateTimeParseException e) {
                throw new InvalidCursorTypeException(cursor, e.getMessage());
            }
        }

        // Pageable 생성
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(0, limit, Sort.by(sortDirection, "createdAt"));

        Slice<Comment> slice = commentRepository.findByReviewIdAndCreatedAtLessThan(reviewId,
            Optional.ofNullable(createdAt).orElse(Instant.now()), pageable);

        List<CommentDto> commentDtos = slice.getContent().stream()
            .map(commentMapper::toDto)
            .toList();

        int size = slice.getSize();

        log.info("[comment] 전체 조회 요청 - reviewId: {}, size: {}", reviewId, commentDtos.size());
        log.debug("[comment] Slice 정보: {}", slice);

        // 커서 처리
        Instant nextCursor = null;
        boolean hasNext = slice.hasNext();

        if (hasNext && !commentDtos.isEmpty()) {
            nextCursor = commentDtos.get(commentDtos.size() - 1).createdAt();
        }

        Long totalElements = commentRepository.countByReviewId(reviewId);

        log.info("[comment] 전체 조회 응답 - reviewId: {}, 결과 개수: {}, nextCursor: {}",
            reviewId, commentDtos.size(), nextCursor);

        CursorPageResponse<CommentDto> commentResponse = new CursorPageResponse<>(
            commentDtos,
            nextCursor != null ? nextCursor.toString() : null,
            nextCursor != null ? nextCursor.toString() : null,
            size,
            totalElements,
            hasNext
        );

        return commentResponse;
    }
}
