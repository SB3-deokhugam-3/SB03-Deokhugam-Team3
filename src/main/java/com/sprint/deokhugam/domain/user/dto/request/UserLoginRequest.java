package com.sprint.deokhugam.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserLoginRequest(
    @NotBlank(message = "이메일 입력은 필수 입니다.")
    @Email
    String email,

    @NotBlank(message = "비밀번호 입력은 필수 입니다.")
    String password
) {

}
