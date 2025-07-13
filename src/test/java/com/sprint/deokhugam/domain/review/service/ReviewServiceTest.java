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
public class ReviewServiceTest {

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

    @Test
    void 유효한_입력일_경우_리뷰를_정상적으로_생성한다() {
        // given
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        String content = "이 책 따봉임";
        double rating = 4.2;
        ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, content, rating);
        Book book = mock(Book.class);
        User user = mock(User.class);
        Review savedReview = new Review(rating, content, book, user);
        ReviewDto expectedDto = ReviewDto.builder()
            .id(UUID.randomUUID())
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
        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(false);
        given(reviewRepository.save(any())).willReturn(savedReview);
        given(reviewMapper.toDto(savedReview)).willReturn(expectedDto);

        // when
        ReviewDto result = reviewService.create(request);

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
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "내용", 4.0);
        given(bookRepository.findById(bookId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> reviewService.create(request));

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
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "내용", 4.0);
        Book mockBook = mock(Book.class);
        given(bookRepository.findById(bookId)).willReturn(Optional.of(mockBook));
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> reviewService.create(request));

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
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ReviewCreateRequest request = new ReviewCreateRequest(bookId, userId, "내용", 4.0);
        Book mockBook = mock(Book.class);
        User mockUser = mock(User.class);
        given(bookRepository.findById(bookId)).willReturn(Optional.of(mockBook));
        given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
        given(reviewRepository.existsByBookIdAndUserId(bookId, userId)).willReturn(true);

        // when
        Throwable thrown = catchThrowable(() -> reviewService.create(request));

        // then
        assertThat(thrown)
            .isInstanceOf(DuplicationReviewException.class);
        then(bookRepository).should().findById(bookId);
        then(userRepository).should().findById(userId);
        then(reviewRepository).should().existsByBookIdAndUserId(bookId, userId);
        then(reviewMapper).shouldHaveNoInteractions();
    }

}
