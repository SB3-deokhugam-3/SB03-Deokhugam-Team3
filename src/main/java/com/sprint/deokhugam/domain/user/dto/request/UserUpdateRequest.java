package com.sprint.deokhugam.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserUpdateRequest(
        @NotBlank(message = "빈칸은 불가능합니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상, 20자 이하이어야 합니다.")
        String nickname
) {
}
