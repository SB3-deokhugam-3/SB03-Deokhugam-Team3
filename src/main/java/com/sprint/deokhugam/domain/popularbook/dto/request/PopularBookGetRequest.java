package com.sprint.deokhugam.domain.popularbook.dto.request;

import com.sprint.deokhugam.global.enums.PeriodType;
import lombok.Builder;

@Builder
public record PopularBookGetRequest(
    PeriodType period,
    String direction,
    String cursor,
    String after,
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
