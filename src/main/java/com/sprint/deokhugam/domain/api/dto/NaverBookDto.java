package com.sprint.deokhugam.domain.api.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record NaverBookDto(
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn,
    String thumbnailImage
) {

}
