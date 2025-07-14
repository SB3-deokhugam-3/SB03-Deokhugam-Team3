package com.sprint.deokhugam.domain.reviewlike.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.reviewlike.dto.data.ReviewLikeDto;
import com.sprint.deokhugam.domain.reviewlike.entity.ReviewLike;
import com.sprint.deokhugam.domain.reviewlike.mapper.ReviewLikeMapper;
import com.sprint.deokhugam.domain.reviewlike.repository.ReviewLikeRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewLikeService 단위 테스트")
public class ReviewLikeServiceImplTest {

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewLikeMapper reviewLikeMapper;

    @InjectMocks
    private ReviewLikeServiceImpl reviewLikeService;

    UUID reviewId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Review review;
    User user;

    @BeforeEach
    void setUp() {
        review = mock(Review.class);
        ReflectionTestUtils.setField(review, "id", reviewId);

        user = mock(User.class);
        ReflectionTestUtils.setField(user, "id", userId);
    }

    @Test
    void 리뷰좋아요가_존재하지않으면_리뷰좋아요를_생성한다() {
        // given
        ReviewLike savedReviewLike = new ReviewLike(review, user);
        ReviewLikeDto reviewLikeDto = new ReviewLikeDto(reviewId, userId, true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(false);
        given(reviewLikeRepository.save(any())).willReturn(savedReviewLike);
        given(reviewLikeMapper.toDto(savedReviewLike)).willReturn(reviewLikeDto);

        // when
        ReviewLikeDto result = reviewLikeService.toggleLike(reviewId, userId);

        // then
        assertThat(result).isEqualTo(reviewLikeDto);
        assertThat(result.liked()).isTrue();
        then(review).should().increaseLikeCount();
        then(reviewRepository).should().findById(reviewId);
        then(userRepository).should().findById(userId);
        then(reviewLikeRepository).should().existsByReviewIdAndUserId(reviewId, userId);
        then(reviewLikeRepository).should().save(any(ReviewLike.class));
        then(reviewLikeMapper).should().toDto(savedReviewLike);
        then(reviewLikeRepository).should(never()).deleteByReviewIdAndUserId(any(), any());
    }

    @Test
    void 리뷰좋아요가_존재하면_리뷰좋아요를_삭제한다() {
        // given
        ReviewLikeDto reviewLikeDto = new ReviewLikeDto(reviewId, userId, false);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)).willReturn(true);

        // when
        ReviewLikeDto result = reviewLikeService.toggleLike(reviewId, userId);

        // then
        assertThat(result.liked()).isFalse();
        then(review).should().decreaseLikeCount();
        then(reviewLikeRepository).should().deleteByReviewIdAndUserId(reviewId, userId);
        then(reviewLikeRepository).should(never()).save(any());
    }

    @Test
    void 존재하지_않는_리뷰이면_리뷰_좋아요_생성에_실패한다() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> reviewLikeService.toggleLike(reviewId, userId));

        // then
        assertThat(thrown)
            .isInstanceOf(ReviewNotFoundException.class);
        then(reviewRepository).should().findById(reviewId);
        then(userRepository).shouldHaveNoInteractions();
        then(reviewLikeRepository).shouldHaveNoInteractions();
        then(reviewLikeMapper).shouldHaveNoInteractions();
    }

    @Test
    void 존재하지_않는_유저라면_리뷰_생성에_실패한다() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Review mockReview = mock(Review.class);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(mockReview));
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> reviewLikeService.toggleLike(reviewId, userId));

        // then
        assertThat(thrown)
            .isInstanceOf(IllegalArgumentException.class);
        then(reviewRepository).should().findById(reviewId);
        then(userRepository).should().findById(userId);
        then(reviewLikeRepository).shouldHaveNoInteractions();
        then(reviewLikeMapper).shouldHaveNoInteractions();
    }
}
