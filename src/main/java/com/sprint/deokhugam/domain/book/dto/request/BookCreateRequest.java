package com.sprint.deokhugam.domain.book.dto.request;

import java.time.LocalDate;

public record BookCreateRequest(
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn
) {

}
