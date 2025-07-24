package com.sprint.deokhugam.domain.popularbook.dto.data;

import java.util.UUID;

public record BookScoreDto(
    UUID bookId,
    String title,
    String author,
    String thumbnailUrl,
    Long reviewCount,
    Double rating
) {

    private static final double REVIEW_COUNT_WEIGHT = 0.4;
    private static final double RATING_WEIGHT = 0.6;

    public double calculateScore() {
        return reviewCount * REVIEW_COUNT_WEIGHT + rating * RATING_WEIGHT;
    }
}
