package com.sprint.deokhugam.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.user.dto.data.UserDto;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.sprint.deokhugam.domain.user.exception.InvalidUserRequestException;
import com.sprint.deokhugam.domain.user.exception.UserNotFoundException;
import com.sprint.deokhugam.domain.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 회원가입_요청시_201을_반환한다() throws Exception {
        // given
        UserCreateRequest request = UserCreateRequest.builder()
                .email("test@example.com")
                .nickname("testuser")
                .password("test1234!")
                .build();

        UserDto userDto = UserDto.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .nickname("testuser")
                .build();

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenReturn(userDto);

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("testuser"));
    }


    @Test
    void 비밀번호_형식이_잘못되면_400_에러를_반환한다() throws Exception {
        List<String> invalidPasswords = List.of(
                "qwerqwerq", //영어 , 숫자, 특수문자 같이 있어야 하는 조건 위반
                "qwer1234",
                "qwer!!!",
                "1234!@#$",
                "1",  //2자 이상 조건 위반
                "" //빈값
        );

        for (String pw : invalidPasswords) {
            String requestJson = String.format("""
                        {
                          "email": "test@example.com",
                          "nickname": "tester",
                          "password": "%s"
                        }
                    """, pw);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void 닉네임_형식이_잘못되면_400_에러를_반환한다() throws Exception {
        List<String> invalidNickNames = List.of(
                "1", //2자 이상 조건 위반
                "" //빈값
        );

        for (String nickname : invalidNickNames) {
            String requestJson = String.format("""
                        {
                          "email": "test@example.com",
                          "nickname": "%s",
                          "password": "testPassword!"
                        }
                    """, nickname);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void 사용자_조회시_200을_반환한다() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UserDto userDto = new UserDto(userId, "testUser", "testUser@test.com", false);
        when(userService.findUser(userId)).thenReturn(userDto);

        // When
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.nickname").value("testUser"))
                .andExpect(jsonPath("$.email").value("testUser@test.com"));
    }

    @Test
    void 존재하지_않은_사용자_조회시_404을_반환한다() throws Exception {
        UUID invalidId = UUID.randomUUID();
        when(userService.findUser(invalidId))
                .thenThrow(new UserNotFoundException("user", "존재하지 않는 사용자 입니다."));

        // when
        ResultActions result = mockMvc.perform(get("/api/users/{userId}", invalidId)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    void 로그인_요청이_정상이라면_200과_사용자_정보를_반환한다() throws Exception {
        // given
        UserDto userDto = UserDto.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .nickname("testUser")
                .build();

        UserLoginRequest loginRequest = new UserLoginRequest("test@test.com", "test1234!");

        when(userService.loginUser(any(UserLoginRequest.class)))
                .thenReturn(userDto);

        // when
        ResultActions result = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // then
        result.andExpectAll(
                status().isOk(),
                jsonPath("$.email").value("test@test.com"),
                jsonPath("$.nickname").value("testUser")
        );
    }

    @Test
    void 사용자_닉네임_수정_요청시_200을_반환한다() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String newNickname = "updatedNickName";

        UserUpdateRequest request = UserUpdateRequest.builder()
                .nickname(newNickname)
                .build();

        UserDto responseDto = UserDto.builder()
                .id(userId)
                .email("test@example.com")
                .nickname(newNickname)
                .build();

        when(userService.updateUserNickName(eq(request), eq(userId)))
                .thenReturn(responseDto);

        // when
        ResultActions result = mockMvc.perform(
                patch("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        result.andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(userId.toString()),
                jsonPath("$.nickname").value(newNickname),
                jsonPath("$.email").value("test@example.com")
        );
    }

    @Test
    void 닉네임_형식이_잘못되면_400을_반환한다() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        List<String> invalidNicknames = List.of(
                "",            // 빈 문자열
                "a",           // 1자
                "a".repeat(21) // 21자 초과
        );

        for (String invalidNickname : invalidNicknames) {
            UserUpdateRequest request = UserUpdateRequest.builder()
                    .nickname(invalidNickname)
                    .build();

            // when
            ResultActions result = mockMvc.perform(patch("/api/users/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isBadRequest());
        }
    }

    @Test
    void 논리삭제_요청시_200과_유저정보_반환() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UserDto deletedUser = UserDto.builder()
                .id(userId)
                .email("deleted@example.com")
                .nickname("deletedUser")
                .isDeleted(true)
                .build();

        when(userService.deleteUser(userId)).thenReturn(deletedUser);

        // when & then

        ResultActions result = mockMvc.perform(delete("/api/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.isDeleted").value(true));
    }

    @Test
    void 물리삭제_요청시_204응답_반환() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        doNothing().when(userService).hardDeleteUser(userId);

        // when & then
        ResultActions result = mockMvc.perform(delete("/api/users/{userId}/hard", userId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNoContent());
    }

    @Test
    void 논리삭제시_존재하지_않는_유저라면_404반환() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        when(userService.deleteUser(userId))
                .thenThrow(new UserNotFoundException("userId", "존재하지 않은 사용자입니다."));

        // when & then
        ResultActions resultActions =
                mockMvc.perform(delete("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    void 물리삭제시_논리삭제되지_않은_유저라면_400반환() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        doThrow(new InvalidUserRequestException("userId", "논리 삭제되지 않은 사용자는 물리 삭제할 수 없습니다."))
                .when(userService).hardDeleteUser(userId);

        // when & then
        ResultActions result = mockMvc.perform(delete("/api/users/{userId}/hard", userId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_INVALID_INPUT_VALUE"));
    }

    @Test
    void 물리삭제시_존재하지_않는_유저라면_404반환() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        doThrow(new UserNotFoundException("userId", "존재하지 않은 사용자입니다."))
                .when(userService).hardDeleteUser(userId);

        // when & then
        ResultActions result = mockMvc.perform(delete("/api/users/{userId}/hard", userId)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }
}