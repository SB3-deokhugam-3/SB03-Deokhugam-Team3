package com.sprint.deokhugam.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.book.ocr.TesseractOcrExtractor;
import com.sprint.deokhugam.domain.user.dto.request.UserCreateRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserLoginRequest;
import com.sprint.deokhugam.domain.user.dto.request.UserUpdateRequest;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserIntegrationTest {

    @MockitoBean
    private TesseractOcrExtractor tesseractOcrExtractor;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;


    @Test
    @DisplayName("회원가입, 로그인, 정보조회, 닉네임수정, 논리삭제, 물리삭제 통합 시나리오")
    void userFullIntegrationScenario() throws Exception {
        // 1. 회원가입
        UserCreateRequest createRequest = new UserCreateRequest(
            "testuser@example.com",
            "testnickname",
            "Password1!"
        );
        String createResponse = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value("testuser@example.com"))
            .andExpect(jsonPath("$.nickname").value("testnickname"))
            .andReturn().getResponse().getContentAsString();
        UUID userId = UUID.fromString(objectMapper.readTree(createResponse).get("id").asText());

        // 2. 로그인 성공
        UserLoginRequest loginRequest = new UserLoginRequest(
            "testuser@example.com",
            "Password1!"
        );
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.email").value("testuser@example.com"));

        // 3. 로그인 실패(잘못된 비밀번호)
        UserLoginRequest wrongLogin = new UserLoginRequest(
            "testuser@example.com",
            "WrongPassword1!"
        );
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongLogin)))
            .andExpect(status().is4xxClientError());

        // 4. 사용자 정보 조회
        mockMvc.perform(get("/api/users/" + userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.email").value("testuser@example.com"))
            .andExpect(jsonPath("$.nickname").value("testnickname"));

        // 5. 닉네임 수정
        UserUpdateRequest updateRequest = new UserUpdateRequest("updatednickname");
        mockMvc.perform(patch("/api/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickname").value("updatednickname"));

        // 6. 논리 삭제
        mockMvc.perform(delete("/api/users/" + userId))
            .andExpect(status().isOk());
        assertThat(userRepository.findById(userId)).isNotPresent();

        // 7. 물리 삭제
        mockMvc.perform(delete("/api/users/" + userId + "/hard"))
            .andExpect(status().isNotFound());
    }
}