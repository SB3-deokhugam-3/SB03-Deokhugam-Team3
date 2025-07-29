package com.sprint.deokhugam.domain.user.service;

import com.sprint.deokhugam.domain.user.dto.data.UserDto;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserUpdateRequest;
import java.util.UUID;

public interface UserService {

    UserDto createUser(UserCreateRequest userCreateRequest);

    UserDto findUser(UUID userId);

    UserDto loginUser(UserLoginRequest userLoginRequest);

    UserDto updateUserNickName(UserUpdateRequest request, UUID userId);

    UserDto deleteUser(UUID userID);

    void hardDeleteUser(UUID userID);
}
