package com.sprint.deokhugam.domain.review.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record ReviewUpdateRequest(

    @NotBlank String content,

    // api는 1부터 프로토타입 화면에는 0.5 선택가능
    @DecimalMin("1.0") @DecimalMax("5.0")
    Double rating
) {

}
