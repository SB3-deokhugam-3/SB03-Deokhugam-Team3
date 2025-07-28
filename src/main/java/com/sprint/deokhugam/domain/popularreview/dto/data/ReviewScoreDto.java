package com.sprint.deokhugam.domain.popularreview.dto.data;

import com.sprint.deokhugam.domain.review.entity.Review;

public record ReviewScoreDto(
    Review review,
    Long commentCount,
    Long likeCount,
    Double score
) {

}
