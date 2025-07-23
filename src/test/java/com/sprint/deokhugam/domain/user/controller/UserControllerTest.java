package com.sprint.deokhugam.domain.user.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.poweruser.dto.PowerUserDto;
import com.sprint.deokhugam.domain.poweruser.service.PowerUserService;
import com.sprint.deokhugam.domain.user.dto.data.UserDto;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.sprint.deokhugam.domain.user.exception.InvalidUserRequestException;
import com.sprint.deokhugam.domain.user.exception.UserNotFoundException;
import com.sprint.deokhugam.domain.user.service.UserService;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PowerUserService powerUserService;

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

    @Test
    void 파워유저_목록_조회시_200을_반환() throws Exception {
        // given
        PowerUserDto powerUser1 = PowerUserDto.builder()
            .userId(UUID.randomUUID())
            .nickname("powerUser1")
            .period("DAILY")
            .rank(1L)
            .score(95.5)
            .reviewScoreSum(80.0)
            .likeCount(15L)
            .commentCount(10L)
            .createdAt(Instant.now())
            .build();

        PowerUserDto powerUser2 = PowerUserDto.builder()
            .userId(UUID.randomUUID())
            .nickname("powerUser2")
            .period("DAILY")
            .rank(2L)
            .score(85.0)
            .reviewScoreSum(70.0)
            .likeCount(12L)
            .commentCount(8L)
            .createdAt(Instant.now())
            .build();

        CursorPageResponse<PowerUserDto> response = new CursorPageResponse<>(
            List.of(powerUser1, powerUser2),
            "3",
            Instant.now().toString(),
            50,
            100L,
            true
        );

        when(powerUserService.getPowerUsersWithCursor(
            eq(PeriodType.DAILY), eq("ASC"), eq(50), isNull(), isNull()))
            .thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/users/power")
            .param("period", "DAILY")
            .param("direction", "ASC")
            .param("limit", "50"));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].nickname").value("powerUser1"))
            .andExpect(jsonPath("$.content[0].rank").value(1))
            .andExpect(jsonPath("$.content[0].score").value(95.5))
            .andExpect(jsonPath("$.content[1].nickname").value("powerUser2"))
            .andExpect(jsonPath("$.content[1].rank").value(2))
            .andExpect(jsonPath("$.nextCursor").value("3"))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andExpect(jsonPath("$.totalElements").value(100));
    }

    @Test
    void 파워유저_목록_조회시_기본값으로_조회() throws Exception {
        // given
        CursorPageResponse<PowerUserDto> response = new CursorPageResponse<>(
            Collections.emptyList(),
            null,
            null,
            50,
            0L,
            false
        );

        when(powerUserService.getPowerUsersWithCursor(
            eq(PeriodType.DAILY), eq("ASC"), eq(50), isNull(), isNull()))
            .thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/users/power"));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.hasNext").value(false));

        verify(powerUserService).getPowerUsersWithCursor(
            PeriodType.DAILY, "ASC", 50, null, null);
    }

    @Test
    void 파워유저_목록_조회시_커서_기반_페이지네이션이_작동() throws Exception {
        // given
        CursorPageResponse<PowerUserDto> response = new CursorPageResponse<>(
            Collections.emptyList(), "next", "after", 20, 100L, true);

        when(powerUserService.getPowerUsersWithCursor(
            eq(PeriodType.WEEKLY), eq("ASC"), eq(20), eq("cursor"), eq("after")))
            .thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/users/power")
            .param("period", "WEEKLY")
            .param("limit", "20")
            .param("cursor", "cursor")
            .param("after", "after"));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.nextCursor").value("next"))
            .andExpect(jsonPath("$.nextAfter").value("after"))
            .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    void 파워유저_목록_조회시_DESC_정렬() throws Exception {
        // given
        CursorPageResponse<PowerUserDto> response = new CursorPageResponse<>(
            Collections.emptyList(), null, null, 50, 0L, false);

        when(powerUserService.getPowerUsersWithCursor(
            eq(PeriodType.DAILY), eq("DESC"), eq(50), isNull(), isNull()))
            .thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/users/power")
            .param("direction", "DESC"));

        // then
        result.andExpect(status().isOk());
        verify(powerUserService).getPowerUsersWithCursor(
            PeriodType.DAILY, "DESC", 50, null, null);
    }

    @Test
    void 파워유저_목록_조회시_잘못된_direction이면_400을_반환() throws Exception {
        when(powerUserService.getPowerUsersWithCursor(
            any(PeriodType.class), eq("INVALID"), anyInt(), any(), any()))
            .thenThrow(new IllegalArgumentException("잘못된 정렬 방향"));

        // when
        ResultActions result = mockMvc.perform(get("/api/users/power")
            .param("direction", "INVALID"));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 파워유저_목록_조회시_전체_기간_필터링이_작동() throws Exception {
        // given
        CursorPageResponse<PowerUserDto> response = new CursorPageResponse<>(
            Collections.emptyList(), null, null, 50, 0L, false);

        when(powerUserService.getPowerUsersWithCursor(
            eq(PeriodType.ALL_TIME), eq("ASC"), eq(50), isNull(), isNull()))
            .thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/users/power")
            .param("period", "ALL_TIME"));

        // then
        result.andExpect(status().isOk());
        verify(powerUserService).getPowerUsersWithCursor(
            PeriodType.ALL_TIME, "ASC", 50, null, null);
    }

    @Test
    void 파워유저_목록_조회시_잘못된_기간_타입이면_400을_반환() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/users/power")
            .param("period", "INVALID"));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 파워유저_목록_조회시_빈_결과를_반환() throws Exception {
        // given
        CursorPageResponse<PowerUserDto> response = new CursorPageResponse<>(
            Collections.emptyList(), null, null, 50, 0L, false);

        when(powerUserService.getPowerUsersWithCursor(
            eq(PeriodType.DAILY),
            eq("ASC"),
            eq(50),
            isNull(),
            isNull()
        )).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/users/power"));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.hasNext").value(false));

        verify(powerUserService).getPowerUsersWithCursor(
            PeriodType.DAILY, "ASC", 50, null, null);
    }



    @Test
    void 파워유저_목록_조회시_최대_사이즈_제한을_확인() throws Exception {
        // given
        CursorPageResponse<PowerUserDto> response = new CursorPageResponse<>(
            Collections.emptyList(), null, null, 100, 0L, false);

        when(powerUserService.getPowerUsersWithCursor(
            eq(PeriodType.DAILY), eq("ASC"), eq(100), isNull(), isNull()))
            .thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/users/power")
            .param("limit", "100"));

        // then
        result.andExpect(status().isOk());
        verify(powerUserService).getPowerUsersWithCursor(
            PeriodType.DAILY, "ASC", 100, null, null);
    }

    @Test
    void 파워유저_목록_조회시_서비스_예외가_발생하면_500을_반환() throws Exception {
        // given
        when(powerUserService.getPowerUsersWithCursor(
            any(PeriodType.class),
            anyString(),
            anyInt(),
            nullable(String.class),
            nullable(String.class)
        )).thenThrow(new RuntimeException("Internal server error"));

        // when
        ResultActions result = mockMvc.perform(get("/api/users/power"));

        // then
        result.andExpect(status().isInternalServerError());
        verify(powerUserService).getPowerUsersWithCursor(
            PeriodType.DAILY, "ASC", 50, null, null);
    }
}