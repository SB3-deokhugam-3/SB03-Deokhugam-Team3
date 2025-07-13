package com.sprint.deokhugam.domain.reviewlike.service;

import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.reviewlike.dto.data.ReviewLikeDto;
import com.sprint.deokhugam.domain.reviewlike.entity.ReviewLike;
import com.sprint.deokhugam.domain.reviewlike.exception.DuplicationReviewLikeException;
import com.sprint.deokhugam.domain.reviewlike.exception.ReviewLikeNotFoundException;
import com.sprint.deokhugam.domain.reviewlike.mapper.ReviewLikeMapper;
import com.sprint.deokhugam.domain.reviewlike.repository.ReviewLikeRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewLikeServiceImpl implements ReviewLikeService {

    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewLikeMapper reviewLikeMapper;

    @Transactional
    @Override
    public ReviewLikeDto toggleLike(UUID reviewId, UUID userId, boolean liked) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> {
                log.warn("[reviewLike] 생성/삭제 실패 - 존재하지 않는 reviewId={}", reviewId);
                throw new ReviewNotFoundException(reviewId);
            });

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("[reviewLike] 생성/삭제 실패 - 존재하지 않는 userId={}", userId);
                throw new IllegalArgumentException();
//                throw new UserNotFoundException();
            });

        boolean alreadyLiked = reviewLikeRepository.existsByReviewIdAndUserId(reviewId,
            userId);

        if (liked) {
            if (alreadyLiked) {
                log.warn("[reviewLike] 생성 실패 - 이미 좋아요를 누른 상태: reviewId={}, userId={}", reviewId,
                    userId);
                throw new DuplicationReviewLikeException(reviewId, userId);
            }

            ReviewLike reviewLike = new ReviewLike(review, user, true);
            ReviewLike savedReviewLike = reviewLikeRepository.save(reviewLike);
            log.info("[reviewLike] 생성 완료 : reviewLikeId={}, reviewId={}, userId={}, isLiked={}",
                savedReviewLike.getId(), reviewId, userId, true);

            return reviewLikeMapper.toDto(savedReviewLike);

        } else {
            if (!alreadyLiked) {
                log.warn("[reviewLike] 삭제 실패 - 좋아요 상태 아님(리뷰 좋아요 미 존재): reviewId={}, userId={}",
                    reviewId, userId);
                throw new ReviewLikeNotFoundException(reviewId, userId);
            }

            reviewLikeRepository.deleteByReviewIdAndUserId(reviewId, userId);
            log.info("[reviewLike] 좋아요 취소 완료: reviewId={}, userId={}", reviewId, userId);
            return new ReviewLikeDto(reviewId, userId, false);
        }

    }

}
