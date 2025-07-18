package com.sprint.deokhugam.domain.popularbook.dto.data;

import com.sprint.deokhugam.global.period.PeriodType;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PopularBookDto(
    UUID id,
    UUID bookId,
    String title,
    String author,
    String thumbnailUrl,
    PeriodType period,
    Long rank,
    Double score,
    Long reviewCount,
    Double rating,
    Instant createdAt
) {

}
