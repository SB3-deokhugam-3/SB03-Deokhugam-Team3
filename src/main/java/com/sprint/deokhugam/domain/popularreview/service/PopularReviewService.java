package com.sprint.deokhugam.domain.popularreview.service;

import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.util.List;
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


    List<PopularReview> savePopularReviewsByPeriod(List<Review> totalReviews,
        PeriodType period, StepContribution contribution, Instant today);
}
