package com.sprint.deokhugam.domain.book.dto.data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookDto(
    UUID id,
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn,
    String thumbnailUrl,
    Long reviewCount,
    Double rating,
    Instant createdAt,
    Instant updatedAt
) {

}
