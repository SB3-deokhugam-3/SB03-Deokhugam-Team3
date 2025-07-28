package com.sprint.deokhugam.domain.user.service;

import com.sprint.deokhugam.domain.user.dto.data.UserDto;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.exception.DuplicateEmailException;
import com.sprint.deokhugam.domain.user.exception.InvalidUserRequestException;
import com.sprint.deokhugam.domain.user.exception.UserNotFoundException;
import com.sprint.deokhugam.domain.user.mapper.UserMapper;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    return new UserNotFoundException(userId, "존재하지 않는 사용자 입니다.");
                });
    }

    @Override
    public UserDto loginUser(UserLoginRequest userLoginRequest) {
        if (userLoginRequest == null) {
            throw new InvalidUserRequestException("null", "null 값으로 로그인 할 수 없습니다.");
        }
        log.debug("[UserService]: 사용자 로그인 요청 : email={}", userLoginRequest.email());

        String email = userLoginRequest.email();
        String password = userLoginRequest.password();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[UserService]: 사용자 조회 실패: email={}", email);
                    return new InvalidUserRequestException("email", "해당하는 이메일은 존재하지 않습니다.");
                });

        if (!user.getPassword().equals(password)) {
            throw new InvalidUserRequestException("password", "비밀번호가 일치하지 않습니다.");
        }

        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateUserNickName(UserUpdateRequest request, UUID userId) {
        if (request == null) {
            throw new InvalidUserRequestException("null", "null값으로 닉네임을 수정할 수 없습니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId, "존재하지 않는 사용자 입니다."));

        user.update(request.nickname());

        return userMapper.toDto(user);

    }

    @Transactional
    @Override
    public UserDto deleteUser(UUID userID) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new UserNotFoundException("userId", "존재하지 않은 사용자입니다."));

        user.softDelete();

        return userMapper.toDto(user);
    }

    @Transactional
    @Override
    public void hardDeleteUser(UUID userID) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new UserNotFoundException("userId", "존재하지 않은 사용자입니다."));

        if (!user.getIsDeleted()) {
            throw new InvalidUserRequestException("userId", "논리 삭제되지 않은 사용자는 물리 삭제할 수 없습니다.");
        }

        userRepository.delete(user);
    }

    private void validateUserCreateRequest(UserCreateRequest userCreateRequest) {
        if (userRepository.existsByEmail(userCreateRequest.email())) {
            throw new DuplicateEmailException(userCreateRequest.email(), "이미 존재하는 이메일입니다.");
        }
    }
}
