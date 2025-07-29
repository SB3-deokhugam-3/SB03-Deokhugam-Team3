package com.sprint.deokhugam.domain.popularreview.dto.data;

import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.util.UUID;

public record PopularReviewDto(
    UUID id,
    UUID reviewId,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl,
    UUID userId,
    String userNickname,
    String reviewContent,
    double reviewRating,
    PeriodType period,
    Instant createdAt,
    Long rank,
    double score,
    Long likeCount,
    Long commentCount
) {

}
