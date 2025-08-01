package com.sprint.deokhugam.domain.popularreview.service;

import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.batch.core.StepContribution;
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

    void validateJobNotDuplicated(Instant referenceTime);

    List<PopularReview> savePopularReviewsByPeriod(PeriodType period, Instant today,
        Map<UUID, Long> commentMap, Map<UUID, Long> likeMap, StepContribution contribution);

    /**
     * 특정 사용자의 기간별 인기 리뷰 점수 합계를 반환
     *
     * @param userId 사용자 ID
     * @param period 기간 타입 ( DAILY, WEEKLY, MONTHLY, ALL )
     * @return 해당 기간 동안 사용자가 작성한 인기 리뷰들의 점수 합계
     */
    Double getUserPopularityScoreSum(UUID userId, PeriodType period);
}
