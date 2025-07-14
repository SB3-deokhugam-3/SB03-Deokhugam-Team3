package com.sprint.deokhugam.domain.review.service;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.DuplicationReviewException;
import com.sprint.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.sprint.deokhugam.domain.review.mapper.ReviewMapper;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
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

    @Override
    public ReviewDto findById(UUID reviewId) {
        log.info("[review] 조회 요청: id={}", reviewId);

        return reviewRepository.findById(reviewId)
            .map(reviewMapper::toDto)
            .orElseThrow(() -> {
                log.warn("[review] 조회 실패 - 존재하지 않는 id: id={}", reviewId);
                return new ReviewNotFoundException(reviewId);
            });
    }
}
