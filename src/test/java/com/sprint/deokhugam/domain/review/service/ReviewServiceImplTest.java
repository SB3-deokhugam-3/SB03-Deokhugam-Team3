package com.sprint.deokhugam.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

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
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 단위 테스트")
public class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    UUID bookId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String content = "이 책 따봉임";
    double rating = 4.2;
    Instant now = Instant.now();

    @Test
    void 유효한_입력일_경우_리뷰를_정상적으로_생성한다() {
        // given
        Book book = mock(Book.class);
        User user = mock(User.class);
        Review savedReview = createReview(book, user);
        ReviewDto expectedDto = createDto(UUID.randomUUID());
        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(false);
        given(reviewRepository.save(any())).willReturn(savedReview);
        given(reviewMapper.toDto(savedReview)).willReturn(expectedDto);

        // when
        ReviewDto result = reviewService.create(createRequest());

        // then
        assertThat(result).isEqualTo(expectedDto);
        then(bookRepository).should().findById(bookId);
        then(userRepository).should().findById(userId);
        then(reviewRepository).should().save(any());
        then(reviewMapper).should().toDto(savedReview);
    }

    @Test
    void 존재하지_않는_책이면_리뷰_생성에_실패한다() {
        // given
        bookId = UUID.randomUUID();
        given(bookRepository.findById(bookId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> reviewService.create(createRequest()));

        // then
        assertThat(thrown)
            .isInstanceOf(IllegalArgumentException.class);
        then(bookRepository).should().findById(bookId);
        then(userRepository).shouldHaveNoInteractions();
        then(reviewRepository).shouldHaveNoInteractions();
        then(reviewMapper).shouldHaveNoInteractions();
    }

    @Test
    void 존재하지_않는_유저라면_리뷰_생성에_실패한다() {
        // given
        userId = UUID.randomUUID();
        Book mockBook = mock(Book.class);
        given(bookRepository.findById(bookId)).willReturn(Optional.of(mockBook));
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> reviewService.create(createRequest()));

        // then
        assertThat(thrown)
            .isInstanceOf(IllegalArgumentException.class);
        then(bookRepository).should().findById(bookId);
        then(userRepository).should().findById(userId);
        then(reviewRepository).shouldHaveNoInteractions();
        then(reviewMapper).shouldHaveNoInteractions();
    }

    @Test
    void 이미_존재하는_리뷰라면_리뷰_생성에_실패한다() {
        // given
        Book mockBook = mock(Book.class);
        User mockUser = mock(User.class);
        given(bookRepository.findById(bookId)).willReturn(Optional.of(mockBook));
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(true);

        // when
        Throwable thrown = catchThrowable(() -> reviewService.create(createRequest()));

        // then
        assertThat(thrown)
            .isInstanceOf(DuplicationReviewException.class);
        then(bookRepository).should().findById(bookId);
        then(userRepository).should().findById(userId);
        then(reviewRepository).should().existsByBookIdAndUserId(bookId, userId);
        then(reviewMapper).shouldHaveNoInteractions();
    }

    @Test
    void 존재하는_리뷰id로_리뷰를_조회할_수_있다() {
        // given
        UUID reviewId = UUID.randomUUID();
        Book book = mock(Book.class);
        User user = mock(User.class);
        Review savedReview = createReview(book, user);
        ReviewDto expectedDto = createDto(reviewId);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(savedReview));
        given(reviewMapper.toDto(savedReview)).willReturn(expectedDto);

        // when
        ReviewDto result = reviewService.findById(reviewId);

        // then
        assertThat(result).isEqualTo(expectedDto);
        then(reviewRepository).should().findById(reviewId);
        then(reviewMapper).should().toDto(savedReview);
    }

    @Test
    void 존재하지않는_리뷰를_조회하면_조회에_실패한다() {
        // given
        UUID reviewId = UUID.randomUUID();
        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> reviewService.findById(reviewId));

        // then
        assertThat(thrown)
            .isInstanceOf(ReviewNotFoundException.class);
    }

    private ReviewCreateRequest createRequest() {
        return new ReviewCreateRequest(bookId, userId, content, rating);
    }

    private Review createReview(Book book, User user) {
        return new Review(rating, content, book, user);
    }

    private ReviewDto createDto(UUID reviewId) {
        return ReviewDto.builder()
            .id(reviewId)
            .bookId(bookId)
            .bookTitle("테스트 책")
            .bookThumbnailUrl("http://image.url")
            .userId(userId)
            .userNickname("테스터")
            .content(content)
            .rating(rating)
            .likeCount(0L)
            .commentCount(0L)
            .likedByMe(false)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

}
