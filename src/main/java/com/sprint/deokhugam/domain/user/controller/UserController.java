package com.sprint.deokhugam.domain.user.controller;

import com.sprint.deokhugam.domain.user.dto.data.UserDto;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sprint.deokhugam.domain.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> create(
            @Valid @RequestBody UserCreateRequest userCreateRequest
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

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> find(
            @PathVariable("userId") UUID userId
    ) {
        log.info("[UserController] 사용자 조회 요청: userId: {}", userId);

        UserDto user = userService.findUser(userId);

        log.info("[UserController] 사용자 조회 완료: id: {}, email: {}",
                user.id(), user.email());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(
            @Valid @RequestBody UserLoginRequest userLoginRequest
    ) {
        log.info("[UserController] 사용자 로그인 요청: email: {}", userLoginRequest.email());

        UserDto loginUser = userService.loginUser(userLoginRequest);

        log.info("[UserController] 사용자 로그인 성공: id: {}, email: {}",
                loginUser.id(), loginUser.email());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(loginUser);
    }
}