package com.sprint.deokhugam.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.dto.request.ReviewCreateRequest;
import com.sprint.deokhugam.domain.review.dto.request.ReviewGetRequest;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.DuplicationReviewException;
import com.sprint.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.sprint.deokhugam.domain.review.mapper.ReviewMapper;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.exception.InvalidTypeException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
    private CursorPageResponse<ReviewDto> mockResponse;
    private List<ReviewDto> mockReviewDtos;
    private List<Review> mockReviews;

    @BeforeEach
    void 초기_설정() {
        /* review Entity 생성 */
        // 공통 시간
        Instant createdAt1 = Instant.parse("2025-01-02T00:00:00Z");
        Instant createdAt2 = Instant.parse("2025-01-03T00:00:00Z");

        // ---------- [USER 1] ----------
        User user1 = User.builder()
            .email("user1@example.com")
            .nickname("유저1")
            .password("encryptedPwd1")
            .build();
        ReflectionTestUtils.setField(user1, "id",
            UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b"));
        ReflectionTestUtils.setField(user1, "createdAt", createdAt1);
        ReflectionTestUtils.setField(user1, "updatedAt", createdAt1);

        // ---------- [BOOK 1] ----------
        Book book1 = Book.builder()
            .title("책1")
            .author("저자1")
            .description("설명1")
            .publisher("출판사1")
            .publishedDate(LocalDate.parse("2022-01-01"))
            .isbn("111-1111111111")
            .thumbnailUrl("https://example.com/image1.jpg")
            .reviewCount(10L)
            .rating(4.5)
            .isDeleted(false)
            .build();
        ReflectionTestUtils.setField(book1, "id",
            UUID.fromString("f6601c1d-c9b9-4ae1-a7aa-b4345921f4ca"));
        ReflectionTestUtils.setField(book1, "createdAt", createdAt1);
        ReflectionTestUtils.setField(book1, "updatedAt", createdAt1);

        // ---------- [REVIEW 1] ----------
        Review review1 = Review.builder()
            .content("리뷰1")
            .rating(0.0)
            .likeCount(10L)
            .commentCount(12L)
            .isDeleted(false)
            .user(user1)
            .book(book1)
            .build();
        ReflectionTestUtils.setField(review1, "id",
            UUID.fromString("cea1a965-2817-4431-90e3-e5701c70d43d"));
        ReflectionTestUtils.setField(review1, "createdAt", createdAt1);
        ReflectionTestUtils.setField(review1, "updatedAt", createdAt1);

        // ---------- [USER 2] ----------
        User user2 = User.builder()
            .email("user2@example.com")
            .nickname("유저2")
            .password("encryptedPwd2")
            .build();
        ReflectionTestUtils.setField(user2, "id",
            UUID.fromString("04e8e411-dd9c-451e-b03e-b393557b283e"));
        ReflectionTestUtils.setField(user2, "createdAt", createdAt2);
        ReflectionTestUtils.setField(user2, "updatedAt", createdAt2);

        // ---------- [BOOK 2] ----------
        Book book2 = Book.builder()
            .title("책2")
            .author("저자2")
            .description("설명2")
            .publisher("출판사2")
            .publishedDate(LocalDate.parse("2021-05-20"))
            .isbn("222-2222222222")
            .thumbnailUrl("https://example.com/image2.jpg")
            .reviewCount(30L)
            .rating(3.8)
            .isDeleted(false)
            .build();
        ReflectionTestUtils.setField(book2, "id",
            UUID.fromString("17fede2c-5df9-4655-999c-03829265850e"));
        ReflectionTestUtils.setField(book2, "createdAt", createdAt2);
        ReflectionTestUtils.setField(book2, "updatedAt", createdAt2);

        // ---------- [REVIEW 2] ----------
        Review review2 = Review.builder()
            .content("리뷰2")
            .rating(0.0)
            .likeCount(382L)
            .commentCount(2L)
            .isDeleted(false)
            .user(user2)
            .book(book2)
            .build();
        ReflectionTestUtils.setField(review2, "id",
            UUID.fromString("044458f4-72a3-49aa-96f8-1a5160f444e2"));
        ReflectionTestUtils.setField(review2, "createdAt", createdAt2);
        ReflectionTestUtils.setField(review2, "updatedAt", createdAt2);

        // ---------- [USER 3] ----------
        User user3 = User.builder()
            .email("user3@example.com")
            .nickname("유저3")
            .password("encryptedPwd3")
            .build();
        ReflectionTestUtils.setField(user3, "id",
            UUID.fromString("92b2771b-59ea-420f-87b0-eafe16ec4321"));
        ReflectionTestUtils.setField(user3, "createdAt", Instant.parse("2025-01-04T00:00:00Z"));
        ReflectionTestUtils.setField(user3, "updatedAt", Instant.parse("2025-01-04T00:00:00Z"));

        // ---------- [BOOK 3] ----------
        Book book3 = Book.builder()
            .title("책3")
            .author("저자3")
            .description("설명3")
            .publisher("출판사3")
            .publishedDate(LocalDate.parse("2020-12-15"))
            .isbn("333-3333333333")
            .thumbnailUrl("https://example.com/image3.jpg")
            .reviewCount(21L)
            .rating(4.2)
            .isDeleted(false)
            .build();
        ReflectionTestUtils.setField(book3, "id",
            UUID.fromString("7c315598-cdbe-491b-a2a7-36b6f1fc9473"));
        ReflectionTestUtils.setField(book3, "createdAt", Instant.parse("2025-01-04T00:00:00Z"));
        ReflectionTestUtils.setField(book3, "updatedAt", Instant.parse("2025-01-04T00:00:00Z"));

        // ---------- [REVIEW 3] ----------
        Review review3 = Review.builder()
            .content("리뷰3")
            .rating(0.0)
            .likeCount(77L)
            .commentCount(6L)
            .isDeleted(false)
            .user(user3)
            .book(book3)
            .build();
        ReflectionTestUtils.setField(review3, "id",
            UUID.fromString("b99bc315-2400-4ff6-8891-9a42f4c31bc4"));
        ReflectionTestUtils.setField(review3, "createdAt", Instant.parse("2025-01-04T00:00:00Z"));
        ReflectionTestUtils.setField(review3, "updatedAt", Instant.parse("2025-01-04T00:00:00Z"));




        /* review DTO 생성 */
        ReviewDto reviewDto1 = ReviewDto.builder()
            .id(UUID.fromString("cea1a965-2817-4431-90e3-e5701c70d43d"))
            .bookId(UUID.fromString("f6601c1d-c9b9-4ae1-a7aa-b4345921f4ca"))
            .userId(UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b"))
            .bookTitle("책1")
            .bookThumbnailUrl("https://example.com/image1.jpg")
            .userNickname("유저1")
            .content("리뷰1")
            .likeCount(10L)
            .commentCount(12L)
            .likedByMe(false)
            .createdAt(Instant.parse("2025-01-02T00:00:00Z"))
            .updatedAt(Instant.parse("2025-01-02T00:00:00Z"))
            .build();

        ReviewDto reviewDto2 = ReviewDto.builder()
            .id(UUID.fromString("044458f4-72a3-49aa-96f8-1a5160f444e2"))
            .bookId(UUID.fromString("17fede2c-5df9-4655-999c-03829265850e"))
            .userId(UUID.fromString("04e8e411-dd9c-451e-b03e-b393557b283e"))
            .bookTitle("책2")
            .bookThumbnailUrl("https://example.com/image2.jpg")
            .userNickname("유저2")
            .content("리뷰2")
            .likeCount(382L)
            .commentCount(2L)
            .likedByMe(true)
            .createdAt(Instant.parse("2025-01-03T00:00:00Z"))
            .updatedAt(Instant.parse("2025-01-03T00:00:00Z"))
            .build();

        ReviewDto reviewDto3 = ReviewDto.builder()
            .id(UUID.fromString("b99bc315-2400-4ff6-8891-9a42f4c31bc4"))
            .bookId(UUID.fromString("7c315598-cdbe-491b-a2a7-36b6f1fc9473"))
            .userId(UUID.fromString("92b2771b-59ea-420f-87b0-eafe16ec4321"))
            .bookTitle("책3")
            .bookThumbnailUrl("https://example.com/image3.jpg")
            .userNickname("유저3")
            .content("리뷰3")
            .likeCount(77L)
            .commentCount(6L)
            .likedByMe(false)
            .createdAt(Instant.parse("2025-01-04T00:00:00Z"))
            .updatedAt(Instant.parse("2025-01-04T00:00:00Z"))
            .build();

        mockReviews = List.of(review1, review2, review3);
        mockReviewDtos = List.of(reviewDto1, reviewDto2, reviewDto3);
        mockResponse = new CursorPageResponse<>(
            mockReviewDtos,
            null,
            null,
            mockReviewDtos.size(),
            (long) mockReviewDtos.size(),
            false
        );
    }


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

    @Test
    @DisplayName("첫 페이지 조회 - 데이터 있을때")
    void 최신순_오름차순으로_리뷰를_전체조회한다() throws Exception {
        //given
        ReviewGetRequest request = ReviewGetRequest.builder()
            .orderBy("createdAt")
            .direction("ASC")
            .limit(2)
            .build();
        given(reviewRepository.findAll(any(ReviewGetRequest.class)))
            .willReturn(mockReviews.subList(0, 3));
        given(reviewRepository.countAllByFilterCondition(any()))
            .willReturn(100L);
        given(reviewMapper.toDto(any(Review.class)))
            .willReturn(mockReviewDtos.get(0), mockReviewDtos.get(1));

        //when
        CursorPageResponse<ReviewDto> result = reviewService.findAll(request,
            UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b"));

        //then
        Assertions.assertThat(result.content()).hasSize(2);
        Assertions.assertThat(result.totalElements()).isEqualTo(100L);
        Assertions.assertThat(result.hasNext()).isTrue();
        Assertions.assertThat(result.nextCursor()).isNotNull();
        Assertions.assertThat(result.nextAfter()).isNotNull();
        then(reviewMapper).should(atLeastOnce()).toDto(any(Review.class));
        then(reviewRepository).should().countAllByFilterCondition(any());
    }

    @Test
    @DisplayName("첫 페이지 조회- 데이터없을때")
    void 데이터가_없을때_최신순_오름차순으로_리뷰를_전체조회한다() throws Exception {
        //given
        ReviewGetRequest request = ReviewGetRequest.builder()
            .orderBy("createdAt")
            .direction("ASC")
            .limit(2)
            .build();
        given(reviewRepository.findAll(any(ReviewGetRequest.class)))
            .willReturn(null);

        //when
        CursorPageResponse<ReviewDto> result = reviewService.findAll(request,
            UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b"));

        CursorPageResponse<ReviewDto> expectedCursorPage = new CursorPageResponse<>(
            new ArrayList<>(),
            null, null, 2,
            0L, false);
        //then
        Assertions.assertThat(result).isEqualTo(expectedCursorPage);
        then(reviewMapper).shouldHaveNoInteractions();
        then(reviewRepository).should(never()).countAllByFilterCondition(any());
    }

    @Test
    @DisplayName("유효하지 않은 정렬 기준 - 예외 발생")
    void 유효하지_않은_정렬_기준_예외_발생() throws Exception {
        //given
        ReviewGetRequest request = ReviewGetRequest.builder()
            .orderBy("invalid")
            .direction("ASC")
            .limit(2)
            .build();
        given(reviewRepository.findAll(any(ReviewGetRequest.class)))
            .willReturn(mockReviews.subList(0, 3));

        //when
        Throwable thrown = catchThrowable(() -> reviewService.findAll(request,
            UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b")));

        // then
        assertThat(thrown)
            .isInstanceOf(InvalidTypeException.class);
    }


}
