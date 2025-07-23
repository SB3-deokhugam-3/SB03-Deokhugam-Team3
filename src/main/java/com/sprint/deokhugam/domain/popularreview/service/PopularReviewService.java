package com.sprint.deokhugam.domain.popularreview.service;

import com.sprint.deokhugam.domain.popularreview.PeriodType;
import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import org.springframework.data.domain.Sort;

public interface PopularReviewService {

    // 조회용
    CursorPageResponse<PopularReviewDto> getPopularReviews(
        PeriodType period,
        Sort.Direction direction,
        String cursor,
        Instant after,
        int limit
    );

//    // 지정된 기간의 인기 리뷰 업데이트
//    void updatePopularReviews(PeriodType periodType);

}