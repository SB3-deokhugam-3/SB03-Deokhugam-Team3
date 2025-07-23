package com.sprint.deokhugam.domain.popularreview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.storage.s3.S3Storage;
import com.sprint.deokhugam.domain.popularreview.PeriodType;
import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.domain.popularreview.mapper.PopularReviewMapper;
import com.sprint.deokhugam.domain.popularreview.repository.PopularReviewRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
public class PopularReviewServiceTest {

    @InjectMocks
    private PopularReviewServiceImpl popularReviewService;

    @Mock
    private PopularReviewRepository popularReviewRepository;

    @Mock
    private PopularReviewMapper popularReviewMapper;

    @Mock
    private S3Storage s3Storage;

    private User user1;
    private User user2;
    private Book book1;
    private Book book2;
    private Review review1;
    private Review review2;
    private Review review3;
    private PopularReview popularReview1;
    private PopularReview popularReview2;
    private PopularReview popularReview3;
    private PopularReviewDto popularReviewDto1;
    private PopularReviewDto popularReviewDto2;
    private PopularReviewDto popularReviewDto3;


    @BeforeEach
    void setup() {
        user1 = User.builder()
            .email("user1@test.com")
            .nickname("유저1")
            .password("pw")
            .build();

        user2 = User.builder()
            .email("user2@test.com")
            .nickname("유저2")
            .password("pw")
            .build();

        book1 = Book.builder()
            .title("테스트 책1")
            .author("저자1")
            .publisher("출판사1")
            .description("테스트 책1 설명")
            .isbn("1234567890")
            .publishedDate(LocalDate.now())
            .thumbnailUrl("http://example.com/book1.jpg")
            .rating(4.5)
            .reviewCount(0L)
            .isDeleted(false)
            .build();

        book2 = Book.builder()
            .title("테스트 책2")
            .author("저자2")
            .publisher("출판사2")
            .description("테스트 책2 설명")
            .isbn("0987654321")
            .publishedDate(LocalDate.now())
            .thumbnailUrl("http://example.com/book2.jpg")
            .rating(4.0)
            .reviewCount(0L)
            .isDeleted(false)
            .build();

        review1 = Review.builder()
            .user(user1)
            .book(book1)
            .content("리뷰1")
            .rating(5)
            .likeCount(10L)
            .commentCount(5L)
            .isDeleted(false)
            .build();

        review2 = Review.builder()
            .user(user2)
            .book(book2)
            .content("리뷰2")
            .rating(4)
            .likeCount(3L)
            .commentCount(2L)
            .isDeleted(false)
            .build();

        review3 = Review.builder()
            .user(user1)
            .book(book2)
            .content("삭제된 리뷰")
            .rating(3)
            .likeCount(1L)
            .commentCount(1L)
            .isDeleted(true)
            .build();

        popularReview1 = PopularReview.builder()
            .review(review1)
            .period(PeriodType.DAILY)
            .rank(1L)
            .score(8.5)
            .likeCount(10L)
            .commentCount(5L)
            .build();

        popularReview2 = PopularReview.builder()
            .review(review2)
            .period(PeriodType.DAILY)
            .rank(2L)
            .score(4.4)
            .likeCount(3L)
            .commentCount(2L)
            .build();

        popularReview3 = PopularReview.builder()
            .review(review3)
            .period(PeriodType.WEEKLY)
            .rank(1L)
            .score(2.0)
            .likeCount(1L)
            .commentCount(1L)
            .build();

        popularReviewDto1 = new PopularReviewDto(
            UUID.randomUUID(),
            review1.getId(),
            book1.getId(),
            book1.getTitle(),
            "https://s3.example.com/converted1.jpg",
            user1.getId(),
            user1.getNickname(),
            review1.getContent(),
            (double) review1.getRating(),
            PeriodType.DAILY,
            Instant.now(),
            1L,
            8.5,
            10L,
            5L
        );

        popularReviewDto2 = new PopularReviewDto(
            UUID.randomUUID(),
            review2.getId(),
            book2.getId(),
            book2.getTitle(),
            "https://s3.example.com/converted2.jpg",
            user2.getId(),
            user2.getNickname(),
            review2.getContent(),
            (double) review2.getRating(),
            PeriodType.DAILY,
            Instant.now(),
            2L,
            4.4,
            3L,
            2L
        );

        popularReviewDto3 = new PopularReviewDto(
            UUID.randomUUID(),
            review3.getId(),
            book2.getId(),
            book2.getTitle(),
            "https://s3.example.com/converted3.jpg",
            user1.getId(),
            user1.getNickname(),
            review3.getContent(),
            (double) review3.getRating(),
            PeriodType.WEEKLY,
            Instant.now(),
            1L,
            2.0,
            1L,
            1L
        );
    }


    @Test
    void getPopularReviews_정상작동() {
        // given
        PeriodType period = PeriodType.DAILY;
        Sort.Direction direction = Sort.Direction.ASC;
        String cursor = null;
        Instant after = Instant.now();
        int limit = 2;
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
            .thenReturn(List.of(popularReview1, popularReview2, popularReview3)); // limit+1개 반환
        when(popularReviewMapper.toDto(eq(popularReview1), eq(s3Storage)))
            .thenReturn(popularReviewDto1);
        when(popularReviewMapper.toDto(eq(popularReview2), eq(s3Storage)))
            .thenReturn(popularReviewDto2);

        // when
        CursorPageResponse<PopularReviewDto> response = popularReviewService.getPopularReviews(
            period, direction, cursor, after, limit
        );

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.content()).containsExactly(popularReviewDto1, popularReviewDto2);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
        assertThat(response.nextAfter()).isEqualTo(popularReviewDto2.createdAt().toString());
    }

    @Test
    void getPopularReviews_hasNext가_false일때() {
        // given
        PeriodType period = PeriodType.DAILY;
        Sort.Direction direction = Sort.Direction.ASC;
        String cursor = null;
        Instant after = Instant.now();
        int limit = 3;
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
            .thenReturn(List.of(popularReview1, popularReview2));
        when(popularReviewMapper.toDto(eq(popularReview1), eq(s3Storage)))
            .thenReturn(popularReviewDto1);
        when(popularReviewMapper.toDto(eq(popularReview2), eq(s3Storage)))
            .thenReturn(popularReviewDto2);

        // when
        CursorPageResponse<PopularReviewDto> response = popularReviewService.getPopularReviews(
            period, direction, cursor, after, limit
        );

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    void getPopularReviews_빈_결과일때() {
        // given
        PeriodType period = PeriodType.DAILY;
        Sort.Direction direction = Sort.Direction.ASC;
        String cursor = null;
        Instant after = Instant.now();
        int limit = 2;
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
            .thenReturn(List.of());

        // when
        CursorPageResponse<PopularReviewDto> response = popularReviewService.getPopularReviews(
            period, direction, cursor, after, limit
        );

        // then
        assertThat(response.content()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextAfter()).isNull();
    }

    @Test
    void generateCursor_정상작동() {
        // given
        PeriodType period = PeriodType.DAILY;
        Sort.Direction direction = Sort.Direction.ASC;
        String cursor = null;
        Instant after = Instant.now();
        int limit = 1;
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
            .thenReturn(List.of(popularReview1, popularReview2));
        when(popularReviewMapper.toDto(eq(popularReview1), eq(s3Storage)))
            .thenReturn(popularReviewDto1);

        // when
        CursorPageResponse<PopularReviewDto> response = popularReviewService.getPopularReviews(
            period, direction, cursor, after, limit
        );

        // then
        assertThat(response.nextCursor()).isNotNull();
        assertThat(response.nextCursor()).matches("^[A-Za-z0-9+/]+=*$");
        String decodedCursor = new String(Base64.getDecoder().decode(response.nextCursor()));
        assertThat(decodedCursor).contains("|");
    }

    @Test
    void 커서_페이징이_순차적으로_동작한다() {
        // given - 첫 번째 페이지
        PeriodType period = PeriodType.DAILY;
        Sort.Direction direction = Sort.Direction.ASC;
        String cursor = null;
        Instant after = null;
        int limit = 1;
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
            .thenReturn(List.of(popularReview1, popularReview2));
        when(popularReviewMapper.toDto(eq(popularReview1), eq(s3Storage)))
            .thenReturn(popularReviewDto1);

        // when - 첫 번째 페이지 조회
        CursorPageResponse<PopularReviewDto> firstPage = popularReviewService.getPopularReviews(
            period, direction, cursor, after, limit
        );

        // then - 첫 번째 페이지 검증
        assertThat(firstPage.content()).hasSize(1);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.nextCursor()).isNotNull();

        // given - 두 번째 페이지 (첫 번째 페이지의 커서 사용)
        String nextCursor = firstPage.nextCursor();
        Instant nextAfter = Instant.parse(firstPage.nextAfter());
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(nextCursor), eq(nextAfter), eq(limit + 1)))
            .thenReturn(List.of(popularReview2)); // 마지막 페이지
        when(popularReviewMapper.toDto(eq(popularReview2), eq(s3Storage)))
            .thenReturn(popularReviewDto2);

        // when
        CursorPageResponse<PopularReviewDto> secondPage = popularReviewService.getPopularReviews(
            period, direction, nextCursor, nextAfter, limit
        );

        // then
        assertThat(secondPage.content()).hasSize(1);
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.nextCursor()).isNull();
    }

    @Test
    void mapper와_s3Storage_연동이_정상작동한다() {
        // given
        PeriodType period = PeriodType.DAILY;
        Sort.Direction direction = Sort.Direction.ASC;
        String cursor = null;
        Instant after = Instant.now();
        int limit = 1;
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
            .thenReturn(List.of(popularReview1));
        when(popularReviewMapper.toDto(eq(popularReview1), eq(s3Storage)))
            .thenReturn(popularReviewDto1);

        // when
        CursorPageResponse<PopularReviewDto> response = popularReviewService.getPopularReviews(
            period, direction, cursor, after, limit
        );

        // then
        assertThat(response.content()).hasSize(1);
        PopularReviewDto dto = response.content().get(0);
        assertThat(dto.bookThumbnailUrl()).isEqualTo("https://s3.example.com/converted1.jpg");
    }
}



