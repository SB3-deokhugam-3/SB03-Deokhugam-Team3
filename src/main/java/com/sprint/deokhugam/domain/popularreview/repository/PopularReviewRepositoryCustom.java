package com.sprint.deokhugam.domain.popularreview.repository;

import com.sprint.deokhugam.domain.popularreview.PeriodType;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Sort;

public interface PopularReviewRepositoryCustom {
//    List<PopularReviewStats> calculatePopularReviews(Instant from, Instant to);

    // 기간별 인기 리뷰 전체 조회(페이징)
    List<PopularReview> findByPeriodWithCursor(
        PeriodType period,
        Sort.Direction direction,
        String cursor,
        Instant after,
        int limit
    );
}
