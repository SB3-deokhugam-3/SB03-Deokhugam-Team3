package com.sprint.deokhugam.domain.review.service;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.exception.BookNotFoundException;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.book.storage.s3.S3Storage;
import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewFeature;
import com.sprint.deokhugam.domain.review.dto.request.ReviewGetRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewUpdateRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.DuplicationReviewException;
import com.sprint.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.sprint.deokhugam.domain.review.exception.ReviewNotSoftDeletedException;
import com.sprint.deokhugam.domain.review.exception.ReviewUnauthorizedAccessException;
import com.sprint.deokhugam.domain.review.mapper.ReviewMapper;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.reviewlike.repository.ReviewLikeRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.exception.UserNotFoundException;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.exception.InvalidTypeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewMapper reviewMapper;
    private final S3Storage s3Storage;

    private static final String ORDER_BY_CREATED_AT = "createdAt";
    private static final String ORDER_BY_RATING = "rating";

    @Override
    public CursorPageResponse<ReviewDto> findAll(ReviewGetRequest params, UUID requestUserId) {
        // 마지막 요소를 한개 더 가져온 후 다음 페이지 있는지 확인
        ReviewGetRequest paramsWithExtraLimit = params.withLimit(params.limit() + 1);
        List<Review> reviewsWithNextCheck = reviewRepository.findAll(paramsWithExtraLimit);

        // 데이터 없으면 바로 return
        if (reviewsWithNextCheck == null || reviewsWithNextCheck.isEmpty()) {
            return new CursorPageResponse<>(new ArrayList<>(),
                null, null, params.limit(),
                0L, false);
        }

        List<Review> reviews = reviewsWithNextCheck;

        // hasNext 값 구하기 + limit값만큼만 데이터 전달
        boolean hasNext = false;
        if (reviewsWithNextCheck.size() > params.limit()) {
            reviews = reviewsWithNextCheck.subList(0, params.limit());
            hasNext = true;
        }

        Review lastReview = reviews.get(reviews.size() - 1);
        String nextCursor = calculateNextCursor(lastReview, params.orderBy());
        String nextAfter = lastReview.getCreatedAt().toString();
        Long totalElements = reviewRepository.countAllByFilterCondition(params);

        List<ReviewDto> reviewDtoList = toDtoWithLikedByMe(reviews, requestUserId);

        return new CursorPageResponse<>(reviewDtoList,
            nextCursor, nextAfter, params.limit(),
            totalElements, hasNext);

    }

    //(createdAt | rating 어떤값을 기준으로 order하는지에 따라 cursor 타입 달라짐)
    private String calculateNextCursor(Review lastReview, String orderBy) {
        return switch (orderBy) {
            case ORDER_BY_CREATED_AT -> lastReview.getCreatedAt().toString();
            case ORDER_BY_RATING -> lastReview.getRating().toString();
            default -> throw new InvalidTypeException("review", Map.of("requestedType", orderBy));
        };
    }

    // ReviewDto에 likeByMe값 동적 추가
    private List<ReviewDto> toDtoWithLikedByMe(List<Review> reviews, UUID requestUserId) {
        // TODO : likedByMe는 나중에 mapper 에서 처리하는게 좋을듯 -> 서비스 관련 로직이라 서비스에 냅둘것

        return reviews.stream().map((review -> {
            ReviewDto reviewDto = reviewMapper.toDto(review, s3Storage);
            boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(review.getId(),
                requestUserId);
            return reviewDto.toBuilder().likedByMe(likedByMe).build();
        })).toList();
    }

    @Transactional
    @Override
    public ReviewDto create(ReviewCreateRequest request) {
        UUID bookId = request.bookId();
        UUID userId = request.userId();
        String content = request.content();
        Integer rating = request.rating();
        log.info("[review] 생성 요청 - bookId: {}, userId: {}", bookId, userId);

        Book book = findByBookId(bookId);
        User user = findByUserId(userId);

        validateDuplicateReview(bookId, userId);

        Review review = new Review(rating, content, book, user);
        Review savedReview = reviewRepository.save(review);
        book.increaseReviewCount();
        updateBookRating(request.bookId());

        log.info("[review] 생성 완료 - reviewId: {}, bookId: {}, userId: {}, rating: {}, content: {}",
            savedReview.getId(), bookId, userId, rating, content);

        return reviewMapper.toDto(savedReview, s3Storage);
    }

    @Override
    public ReviewDto findById(UUID reviewId, UUID requestUserId) {
        log.info("[review] 조회 요청: id={}", reviewId);
        Review review = findByReviewId(reviewId);
        ReviewDto reviewDto = reviewMapper.toDto(review, s3Storage);

        boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, requestUserId);
        return reviewDto.toBuilder().likedByMe(likedByMe).build();
    }

    @Transactional
    @Override
    public void delete(UUID reviewId, UUID userId) {
        Review review = findByReviewId(reviewId);

        validateAuthorizedUser(review, userId, ReviewFeature.SOFT_DELETE);

        review.softDelete();
        review.getBook().decreaseReviewCount();
        updateBookRating(review.getBook().getId());

    }

    @Transactional
    @Override
    public void hardDelete(UUID reviewId, UUID userId) {
        Review review = reviewRepository.findDeletedById(reviewId)
            .orElseThrow(() -> {
                log.warn("[review] 하드 삭제 실패 - 논리 삭제되지 않은 리뷰: {}", reviewId);
                throw new ReviewNotSoftDeletedException(reviewId);
            });

        validateAuthorizedUser(review, userId, ReviewFeature.HARD_DELETE);

        reviewRepository.delete(review);

    }

    @Transactional
    @Override
    public ReviewDto update(UUID reviewId, UUID userId, ReviewUpdateRequest request) {
        Review review = findByReviewId(reviewId);
        validateAuthorizedUser(review, userId, ReviewFeature.UPDATE);
        log.info("[review] 수정 요청 - reviewId: {}, userId: {}", reviewId, userId);

        review.update(request.content(), request.rating());
        log.info("[review] 수정 완료 - reviewId: {}, userId: {}, newContent={}, newRating={}", reviewId,
            userId, request.content(), request.rating());

        updateBookRating(review.getBook().getId());

        return reviewMapper.toDto(review, s3Storage);
    }

    private void updateBookRating(UUID bookId) {
        // 도서 엔티티의 평점 업데이트
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(bookId));

        // 해당 도서의 모든 리뷰 평점 평균 계산
        Double averageRating = reviewRepository.findAverageRatingByBook(book);

        book.updateRating(averageRating != null ? averageRating : 0.0);
        bookRepository.save(book);
    }

    // 검증 메서드
    private Book findByBookId(UUID bookId) {
        return bookRepository.findById(bookId)
            .orElseThrow(() -> {
                log.warn("[review] 생성 실패 - 존재하지 않는 bookId: {}", bookId);
                return new BookNotFoundException(bookId);
            });
    }

    private User findByUserId(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("[review] 생성 실패 - 존재하지 않는 userId: {}", userId);
                return new UserNotFoundException(userId, "존재하지 않는 userId");
            });
    }

    private void validateAuthorizedUser(Review review, UUID userId, ReviewFeature feature) {
        if (!review.getUser().getId().equals(userId)) {
            log.warn("[review] {} - 해당 유저는 권한이 없음: reviewId={}, userId={}", feature.getMessage(),
                review.getId(),
                userId);
            throw new ReviewUnauthorizedAccessException(review.getId(), userId);
        }
    }

    private Review findByReviewId(UUID reviewId) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(() -> {
                log.warn("[review] 조회 실패 - 존재하지 않는 id: {}", reviewId);
                throw new ReviewNotFoundException(reviewId);
            });
    }

    private void validateDuplicateReview(UUID bookId, UUID userId) {
        if (reviewRepository.existsByBookIdAndUserId(bookId, userId)) {
            log.warn("[review] 생성 실패 - 해당 review가 이미 존재함 bookId: {}, userId: {}", bookId, userId);
            throw new DuplicationReviewException(bookId, userId);
        }
    }

}
