package com.sprint.deokhugam.domain.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
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

    /*테스트 초기값 설정 */
    private Instant createdAt;
    private Instant updatedAt;
    private UUID commentId;
    private UUID userId;
    private UUID reviewId;
    private String content;
    private CommentDto expectedDto;

    @BeforeEach
    void 초기_설정() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        userId = UUID.randomUUID();
        reviewId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        content = "댓글테스트";

        expectedDto = CommentDto.builder()
            .id(commentId)
            .content(content)
            .reviewId(reviewId)
            .userId(userId)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }

    @Test
    void 댓글_생성_요청시_201응답_반환() throws Exception {
        //given
        CommentCreateRequest validRequest = new CommentCreateRequest(reviewId, userId,
            content);
        given(commentService.create(any())).willReturn(expectedDto);

        //when
        ResultActions result = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
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
        CommentCreateRequest inValidRequest = new CommentCreateRequest(reviewId, userId,
            "");

        //when
        ResultActions result = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inValidRequest))
        );

        //then
        result.andExpect(status().is4xxClientError());
    }

    @Test
    void 댓글_생성_요청시_reviewId가_없다면_400에러_반환() throws Exception {
        //given
        CommentCreateRequest createRequest = new CommentCreateRequest(null, userId,
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
        CommentCreateRequest createRequest = new CommentCreateRequest(reviewId, null,
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
        given(commentService.findById(any())).willReturn(expectedDto);

        //when
        ResultActions result = mockMvc.perform(
            get("/api/comments/" + commentId)
        );

        //then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(commentId.toString()))
            .andExpect(jsonPath("$.content").value(content));
    }

    @Test
    void commentId없이_댓글_세부정보_요청시_404_에러_반환() throws Exception {
        //given
        given(commentService.findById(any())).willReturn(expectedDto);

        //when
        ResultActions result = mockMvc.perform(
            get("/api/comments/")
        );

        //then
        result.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NO_RESOURCE_FOUND"));
    }

    @Test
    void 댓글_수정_요청시_200성공_반환() throws Exception {
        //given
        given(commentService.updateById(any(), any(), any())).willReturn(expectedDto);
        CommentUpdateRequest validRequest = new CommentUpdateRequest(content);

        //when
        ResultActions result = mockMvc.perform(
            patch("/api/comments/" + commentId)
                .header("Deokhugam-Request-User-ID", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
        );

        //then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(commentId.toString()))
            .andExpect(jsonPath("$.content").value(content));
    }

    @Test
    void 헤더에_DeokhugamRequestUserID가_없을때_댓글_수정_요청시_에러_반환() throws Exception {
        //given
        CommentUpdateRequest validRequest = new CommentUpdateRequest(content);

        //when
        ResultActions result = mockMvc.perform(
            patch("/api/comments/" + commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
        );

        //then
        result.andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("MISSING_REQUEST_HEADER"));
    }

    @Test
    void 수정하려는_텍스트가_빈값일때_댓글_수정_요청시_에러_반환() throws Exception {
        //given
        CommentUpdateRequest inValidRequest = new CommentUpdateRequest("");

        //when
        ResultActions result = mockMvc.perform(
            patch("/api/comments/" + commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inValidRequest))
        );

        //then
        result.andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
    }

    @Test
    void 수정하려는_commentId가_없을때_댓글_수정_요청시_에러_반환() throws Exception {
        //given
        given(commentService.updateById(any(), any(), any())).willReturn(expectedDto);
        CommentUpdateRequest validRequest = new CommentUpdateRequest(content);

        //when
        ResultActions result = mockMvc.perform(
            patch("/api/comments/")
                .header("Deokhugam-Request-User-ID", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
        );

        //then
        result.andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("NO_RESOURCE_FOUND"));
    }
}
