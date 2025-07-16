package com.sprint.deokhugam.domain.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.service.CommentService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(CommentController.class)
@DisplayName("CommentController 슬라이스 테스트")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Instant createdAt;
    private Instant updatedAt;

    @BeforeEach
    void 초기_설정() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @Test
    void 댓글_생성_요청시_201응답_반환() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        String content = "댓글생성테스트";
        CommentCreateRequest createRequest = new CommentCreateRequest(reviewId, userId,
            content);
        CommentDto expectedDto = CommentDto.builder()
            .id(commentId)
            .content(content)
            .reviewId(reviewId)
            .userId(userId)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
        given(commentService.create(any())).willReturn(expectedDto);

        //when
        ResultActions result = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        );

        //then
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(commentId.toString()))
            .andExpect(jsonPath("$.content").value(content))
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.reviewId").value(reviewId.toString()));
    }


    @Test
    void 댓글_생성_요청시_content_가_빈문자면_400에러_반환() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        String content = "";
        CommentCreateRequest createRequest = new CommentCreateRequest(reviewId, userId,
            content);

        //when
        ResultActions result = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        );

        //then
        result.andExpect(status().is4xxClientError());
    }

    @Test
    void 댓글_생성_요청시_reviewId가_없다면_400에러_반환() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        UUID reviewId = null;
        String content = "댓글생성테스트";
        CommentCreateRequest createRequest = new CommentCreateRequest(reviewId, userId,
            content);

        //when
        ResultActions result = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        );

        //then
        result.andExpect(status().is4xxClientError());
    }

    @Test
    void 댓글_생성_요청시_userId가_없다면_400에러_반환() throws Exception {
        //given
        UUID userId = null;
        UUID reviewId = UUID.randomUUID();
        String content = "댓글생성테스트";
        CommentCreateRequest createRequest = new CommentCreateRequest(reviewId, userId,
            content);

        //when
        ResultActions result = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        );

        //then
        result.andExpect(status().is4xxClientError());
    }

    @Test
    void 댓글_세부정보_요청시_200성공_반환() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        CommentDto expectedDto = CommentDto.builder()
            .id(commentId)
            .content("test")
            .reviewId(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
        given(commentService.findById(any())).willReturn(expectedDto);

        //when
        ResultActions result = mockMvc.perform(
            get("/api/comments/" + commentId)
        );

        //then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(commentId.toString()))
            .andExpect(jsonPath("$.content").value("test"));
    }

    @Test
    void commentId없이_댓글_세부정보_요청시_404_에러_반환() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        CommentDto expectedDto = CommentDto.builder()
            .id(commentId)
            .content("test")
            .reviewId(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
        given(commentService.findById(any())).willReturn(expectedDto);

        //when
        ResultActions result = mockMvc.perform(
            get("/api/comments/")
        );

        //then
        result.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NO_RESOURCE_FOUND"));
    }
}
