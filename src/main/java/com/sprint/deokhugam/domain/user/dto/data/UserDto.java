package com.sprint.deokhugam.domain.user.dto.data;

import java.util.UUID;
import lombok.Builder;

@Builder
public record UserDto(
    UUID id,
    String nickname,
    String email
){
}
