package com.sprint.deokhugam.domain.user.service;

import com.sprint.deokhugam.domain.user.dto.data.UserDto;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import java.util.UUID;

public interface UserService {
    UserDto createUser(UserCreateRequest userCreateRequest);

    UserDto findUser(UUID userId);
}
