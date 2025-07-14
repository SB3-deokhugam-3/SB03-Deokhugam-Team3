package com.sprint.deokhugam.domain.review.service;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.DuplicationReviewException;
import com.sprint.deokhugam.domain.review.mapper.ReviewMapper;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import com.sprint.deokhugam.global.exception.NotFoundException;
import com.sprint.deokhugam.global.exception.UnauthorizedException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    @Override
    public ReviewDto create(ReviewCreateRequest request) {
        UUID bookId = request.bookId();
        UUID userId = request.userId();
        log.info("[review] 생성 요청: bookId={}, userId={}", bookId, userId);

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> {
                log.warn("[review] 생성 실패 - 존재하지 않는 bookId={}", bookId);
                return new IllegalArgumentException();
//                return new BookNotFoundException(bookId);
            });

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("[review] 생성 실패 - 존재하지 않는 userId={}", userId);
                return new IllegalArgumentException();
//                return new UserNotFoundException(userId);
            });

        if (reviewRepository.existsByBookIdAndUserId(bookId, userId)) {
            log.warn("[review] 생성 실패 - 해당 review가 이미 존재함: bookId={}, userId={}", bookId, userId);
            throw new DuplicationReviewException(bookId, userId);
        }

        String content = request.content();
        double rating = request.rating();

        Review review = new Review(rating, content, book, user);
        Review savedReview = reviewRepository.save(review);
        log.info("[review] 생성 완료 : reviewId={}, bookId={}, userId={}, rating={}, content={}",
            savedReview.getId(), bookId, userId, rating, content);

        return reviewMapper.toDto(savedReview);
    }

    @Transactional
    @Override
    public HttpStatus delete(UUID reviewId, UUID userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new NotFoundException("review",
                Map.of("reviewId", reviewId)));

        /*삭제할 권한이 없는 사용자일때 */
        if (!review.getUser().getId().equals(userId)) {
            log.warn("[review] 리뷰 삭제 실패 - 해당 유저는 권한이 없음: reviewId={}, userId={}", reviewId, userId);
            throw new UnauthorizedException("review",
                Map.of("userId", userId));
        }
        review.softDelete();

        return HttpStatus.NO_CONTENT;
    }

    @Transactional
    @Override
    public HttpStatus hardDelete(UUID reviewId, UUID userId) {
        Review review = reviewRepository.findDeletedById(reviewId)
            .orElseThrow(() -> new NotFoundException("review",
                Map.of("reviewId", reviewId)));

        /*삭제할 권한이 없는 사용자일때 */
        if (!review.getUser().getId().equals(userId)) {
            log.warn("[review] 리뷰 하드 삭제 실패 - 해당 유저는 권한이 없음: reviewId={}, userId={}", reviewId,
                userId);
            throw new UnauthorizedException("review",
                Map.of("userId", userId));
        }
        reviewRepository.delete(review);

        return HttpStatus.NO_CONTENT;
    }
}
