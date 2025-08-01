package com.sprint.deokhugam.domain.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReviewCreateRequest(

    @NotNull UUID bookId,

    @NotNull UUID userId,

    @NotBlank String content,

    @Min(1) @Max(5)
    Integer rating

) {

}
