package com.sprint.deokhugam.domain.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record BookCreateRequest(
    @NotBlank
    @Size(max = 100)
    String title,

    @NotBlank
    @Size(max = 50)
    String author,

    @NotBlank
    String description,

    @NotBlank
    @Size(max = 50)
    String publisher,

    @NotNull
    @PastOrPresent(message = "출간일은 현재 또는 과거 날짜만 입력 가능합니다.")
    LocalDate publishedDate,

    @Size(max = 13, message = "isbn은 13자까지만 입력 가능합니다.")
    String isbn
) {

}
