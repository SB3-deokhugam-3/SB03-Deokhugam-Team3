package com.sprint.deokhugam.domain.user.mapper;

import com.sprint.deokhugam.domain.user.dto.data.UserDto;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import com.sprint.deokhugam.domain.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // UserCreateRequest → User
    User toEntity(UserCreateRequest request);

    // User → UserDto
    UserDto toDto(User user);
}