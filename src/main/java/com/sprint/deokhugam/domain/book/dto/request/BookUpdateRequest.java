package com.sprint.deokhugam.domain.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record BookUpdateRequest(
    @NotBlank
    @Size(max = 100, message = "제목은 100자까지만 입력 가능합니다.")
    String title,

    @NotBlank
    @Size(max = 50, message = "저자는 50자까지만 입력 가능합니다.")
    String author,

    @NotBlank
    String description,

    @NotBlank
    @Size(max = 50, message = "출판사는 50자까지만 입력 가능합니다.")
    String publisher,

    @NotNull
    @PastOrPresent(message = "출간일은 현재 또는 과거 날짜만 입력 가능합니다.")
    LocalDate publishedDate
) {

}
