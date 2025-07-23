package com.sprint.deokhugam.popularreview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sprint.deokhugam.domain.popularreview.PeriodType;
import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.popularreview.repository.PopularReviewRepository;
import com.sprint.deokhugam.domain.popularreview.service.PopularReviewServiceImpl;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import java.util.List;
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


//    @Test
//    @DisplayName("updatePopularReviews: 기간 내 좋아요/댓글 기반 순위 저장")
//    void updatePopularReviews() {
//        // given
//        UUID reviewId1 = UUID.randomUUID();
//        UUID reviewId2 = UUID.randomUUID();
//
//        List<PopularReviewStats> stats = new ArrayList<>();
//        stats.add(new PopularReviewStats(reviewId1, 10L, 5L));
//        stats.add(new PopularReviewStats(reviewId2, 5L, 10L));
//        Review mockReview1 = mock(Review.class);
//        Review mockReview2 = mock(Review.class);
//
//        given(popularReviewRepository.calculatePopularReviews(any(), any()))
//            .willReturn(stats);
//
//        // when
//        popularReviewService.updatePopularReviews(PeriodType.DAILY);
//
//        // then
//        then(popularReviewRepository).should().saveAll(argThat(iterable -> {
//            List<PopularReview> list = StreamSupport.stream(iterable.spliterator(), false)
//                .collect(Collectors.toList());
//
//            if (list.size() != 2) return false;
//            PopularReview r1 = list.get(0);
//            PopularReview r2 = list.get(1);
//            return r1.getRank() == 1 && r2.getRank() == 2;
//        }));
//    }

    @Test
    void getPopularReviews_정상작동() {
        // given
        PeriodType period = PeriodType.DAILY;
        Sort.Direction direction = Sort.Direction.ASC;
        String cursor = null;
        Instant after = Instant.now();
        int limit = 2;

        PopularReviewDto dto1 = mock(PopularReviewDto.class);
        PopularReviewDto dto2 = mock(PopularReviewDto.class);
        PopularReviewDto dto3 = mock(PopularReviewDto.class);

        Instant createdAt = Instant.now();
        when(dto2.createdAt()).thenReturn(createdAt);
        when(popularReviewRepository.findByPeriodWithCursor(
            eq(period), eq(direction), eq(cursor), eq(after), eq(limit + 1)))
            .thenReturn(List.of(dto1, dto2, dto3));

        // when
        CursorPageResponse<PopularReviewDto> response = popularReviewService.getPopularReviews(
            period, direction, cursor, after, limit
        );

        // then
        assertThat(response.content()).containsExactly(dto1, dto2);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
        assertThat(response.nextAfter()).isEqualTo(createdAt.toString());
    }

}
