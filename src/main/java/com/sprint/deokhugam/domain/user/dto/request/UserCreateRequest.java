package com.sprint.deokhugam.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserCreateRequest(
        @NotBlank
        @Email(message = "유효한 이메일 주소를 입력해주세요.")
        String email,

        @NotBlank
        @Size(min = 2, max = 50)
        String nickname,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,100}$",
                message = "비밀번호는 영어, 숫자, 특수문자를 포함해야 합니다."
        )
        String password
) {

}
