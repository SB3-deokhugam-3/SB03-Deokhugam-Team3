package com.sprint.deokhugam.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.user.dto.data.UserDto;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import com.sprint.deokhugam.domain.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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

        Mockito.when(userService.createUser(any(UserCreateRequest.class)))
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
                ""
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
                ""
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
}