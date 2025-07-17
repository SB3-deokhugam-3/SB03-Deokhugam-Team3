package com.sprint.deokhugam.domain.notification.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NotificationGetRequest(

        UUID userId,
        String direction,
        UUID cursor,
        Instant after,
        //임의로 지정
        @Min(value = 1, message = "최소 1개의 항목은 요청해야 합니다.")
        @Max(value = 100, message = "최대 100개의 항목만 요청할 수 있습니다.")
        Integer limit

) {
    public NotificationGetRequest {
        if (direction == null) {
            direction = "DESC";
        }
        if (limit == null) {
            limit = 20;
        }
    }
}