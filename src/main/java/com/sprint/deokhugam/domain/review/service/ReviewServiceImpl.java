package com.sprint.deokhugam.domain.review.service;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewGetRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.DuplicationReviewException;
import com.sprint.deokhugam.domain.review.mapper.ReviewMapper;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.exception.InvalidTypeException;
import com.sprint.deokhugam.global.exception.NotFoundException;
import com.sprint.deokhugam.global.exception.UnauthorizedException;
import java.util.ArrayList;
import java.util.List;
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

    private static final String ORDER_BY_CREATED_AT = "createdAt";
    private static final String ORDER_BY_RATING = "rating";

    @Override
    public CursorPageResponse<ReviewDto> findAll(ReviewGetRequest params, UUID requestUserId) {
        // 마지막 요소를 한개 더 가져온 후 다음 페이지 있는지 확인
        ReviewGetRequest paramsWithExtraLimit = params.toBuilder().limit(params.getLimit() + 1)
            .build();
        List<Review> reviewsWithNextCheck = reviewRepository.findAll(paramsWithExtraLimit);

        // 데이터 없으면 바로 return
        if (reviewsWithNextCheck == null || reviewsWithNextCheck.isEmpty()) {
            return new CursorPageResponse<>(new ArrayList<>(),
                null, null, params.getLimit(),
                0L, false);
        }

        List<Review> reviews = reviewsWithNextCheck;

        // hasNext 값 구하기 + limit값만큼만 데이터 전달
        boolean hasNext = false;
        if (reviewsWithNextCheck.size() > params.getLimit()) {
            reviews = reviewsWithNextCheck.subList(0, params.getLimit());
            hasNext = true;
        }

        Review lastReview = reviews.get(reviews.size() - 1);
        String nextCursor = calculateNextCursor(lastReview, params.getOrderBy());
        String nextAfter = lastReview.getCreatedAt().toString();
        Long totalElements = reviewRepository.countAllByFilterCondition(params);

        List<ReviewDto> reviewDtoList = toDtoWithLikedByMe(reviews, requestUserId);

        return new CursorPageResponse<>(reviewDtoList,
            nextCursor, nextAfter, params.getLimit(),
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
        boolean likedByMe = false;

        return reviews.stream().map((review -> {
            ReviewDto reviewDto = reviewMapper.toDto(review);
            //TODO: review-like 테이블 생성 후 query 로 가져올것
            return reviewDto.toBuilder().likedByMe(likedByMe).build();
        })).toList();
    }


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
            .orElseThrow(() -> {
                log.warn("[review] 리뷰 삭제 실패 - 해당 리뷰 없음: reviewId={}", reviewId);
                return new NotFoundException("review",
                    Map.of("reviewId", reviewId));
            });

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
