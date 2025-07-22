package com.sprint.deokhugam.domain.popularbook.dto.request;

import com.sprint.deokhugam.global.period.PeriodType;
import java.time.Instant;
import lombok.Builder;

@Builder
public record PopularBookGetRequest(
    PeriodType period,
    String direction,
    String cursor,
    Instant after,
    Integer limit
) {
    public PopularBookGetRequest {
        if (period == null) {
            period = PeriodType.DAILY;
        }
        if (direction == null) {
            direction = "ASC";
        }
        if (limit == null) {
            limit = 50;
        }
    }
}
