package com.sprint.deokhugam.domain.user.controller;

import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.user.controller.api.UserApi;
import com.sprint.deokhugam.domain.user.dto.data.UserDto;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.sprint.deokhugam.domain.user.service.UserService;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController implements UserApi {

    private final UserService userService;

    public ResponseEntity<UserDto> create(
        UserCreateRequest userCreateRequest
    ) {
        log.info("[UserController] 사용자 회원가입 요청: email: {}, nickname: {}",
            userCreateRequest.email(), userCreateRequest.nickname());

        UserDto createdUser = userService.createUser(userCreateRequest);

        log.info("[UserController] 사용자 회원가입 완료: id: {}, email: {}",
            createdUser.id(), createdUser.email());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdUser);
    }

    public ResponseEntity<UserDto> find(UUID userId) {
        log.info("[UserController] 사용자 조회 요청: userId: {}", userId);

        UserDto user = userService.findUser(userId);

        log.info("[UserController] 사용자 조회 완료: id: {}, email: {}",
            user.id(), user.email());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(user);
    }

    public ResponseEntity<UserDto> login(UserLoginRequest userLoginRequest) {
        log.info("[UserController] 사용자 로그인 요청: email: {}", userLoginRequest.email());

        UserDto loginUser = userService.loginUser(userLoginRequest);

        log.info("[UserController] 사용자 로그인 성공: id: {}, email: {}",
            loginUser.id(), loginUser.email());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(loginUser);
    }

    public ResponseEntity<UserDto> update(UUID userId, UserUpdateRequest userUpdateRequest) {
        log.info("[UserController] 사용자 닉네임 수정: userId: {}, nickname: {}", userId,
            userUpdateRequest.nickname());

        UserDto updateUser = userService.updateUserNickName(userUpdateRequest, userId);

        log.info("[UserController] 사용자 닉네임 수정 성공: userId: {}, nickname: {}", userId,
            userUpdateRequest.nickname());
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(updateUser);
    }

    public ResponseEntity<Void> hardDelete(UUID userId) {
        log.info("[UserController] 물리 삭제 요청: userId: {}", userId);

        userService.hardDeleteUser(userId);

        log.info("[UserController] 물리 삭제 성공: userId: {}", userId);

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    public ResponseEntity<UserDto> deleted(UUID userId) {
        log.info("[UserController] 논리 삭제 요청: userId: {}", userId);

        UserDto deletedUser = userService.deleteUser(userId);

        log.info("[UserController] 논리 삭제 성공: userId: {}", userId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(deletedUser);
    }

    public ResponseEntity<CursorPageResponse<PopularReviewDto>> getPopularUsers(
        PeriodType period, String direction, String cursor, Instant after, int limit
    ) {

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(null);
    }
}