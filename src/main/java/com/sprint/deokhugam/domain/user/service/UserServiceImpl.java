package com.sprint.deokhugam.domain.user.service;

import com.sprint.deokhugam.domain.user.dto.data.UserDto;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.exception.DuplicateEmailException;
import com.sprint.deokhugam.domain.user.exception.UserNotFoundException;
import com.sprint.deokhugam.domain.user.mapper.UserMapper;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserCreateRequest userCreateRequest) {

        log.debug("[UserService]: 사용자 등록 요청 - UserCreateRequest: {}", userCreateRequest);

        validateUserCreateRequest(userCreateRequest);

        User user = userMapper.toEntity(userCreateRequest);
        User saved = userRepository.save(user);

        log.info("[UserService]: 사용자 등록 완료: id={}, nickname={}", saved.getId(), saved.getNickname());

        return userMapper.toDto(saved);

    }

    @Override
    public UserDto findUser(UUID userId) {

        log.debug("[UserService]: 사용자 조회 요청: id={}", userId);

        return userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> {
                    log.warn("[UserService]: 사용자 조회 실패: id={}", userId);
                    return new UserNotFoundException("user", "존재하지 않는 사용자 입니다.");
                });
    }

    private void validateUserCreateRequest(UserCreateRequest userCreateRequest) {
        if (userRepository.existsByEmail(userCreateRequest.email())) {
            throw new DuplicateEmailException(userCreateRequest.email(), "이미 존재하는 이메일입니다.");
        }
    }
}
