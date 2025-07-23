package com.sprint.deokhugam.domain.popularreview.util;

import java.util.UUID;

public record PopularReviewStats (
    UUID reviewId,
    Long likeCount,
    Long commentCount

){
    private static final double LIKE_WEIGHT = 0.3;
    private static final double COMMENT_WEIGHT = 0.7;

    public double score() {
        return (likeCount * LIKE_WEIGHT) + (commentCount * COMMENT_WEIGHT);
    }

}
