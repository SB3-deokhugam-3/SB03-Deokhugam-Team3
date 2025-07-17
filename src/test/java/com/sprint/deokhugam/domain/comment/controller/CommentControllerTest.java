package com.sprint.deokhugam.domain.comment.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.deokhugam.domain.comment.exception.InvalidCursorTypeException;
import com.sprint.deokhugam.domain.comment.service.CommentService;
import com.sprint.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ActiveProfiles("test")
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

    @Test
    void 커서_기반_댓글_목록_조회에_성공하면_200응답을_반환한다() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String direction = "DESC";
        int limit = 2;
        String nextCursor = Instant.now().toString();

        List<CommentDto> comments = List.of(
            new CommentDto(UUID.randomUUID(), reviewId, userId, "조조", "댓글1", Instant.now(), null),
            new CommentDto(UUID.randomUUID(), reviewId, userId, "조조", "댓글2", Instant.now(), null)
        );

        CursorPageResponse<CommentDto> response = new CursorPageResponse<>(
            comments,
            nextCursor,
            nextCursor,
            comments.size(),
            10L,
            true
        );

        given(commentService.findAll(eq(reviewId), isNull(), eq(direction), eq(limit)))
            .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/comments")
                .param("reviewId", reviewId.toString())
                .param("direction", direction)
                .param("limit", String.valueOf(limit)));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andExpect(jsonPath("$.totalElements").value(10));
    }

    @Test
    void 댓글_목록_조회시_reviewId가_없으면_400_반환() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/comments")
            .param("direction", "DESC")
            .param("limit", "10"));

        // then
        result.andExpect(status().isBadRequest())
            .andDo(print());
        verify(commentService, never()).findAll(any(), any(), any(), anyInt());
    }

    @Test
    void 댓글_목록_조회시_존재하지_않는_리뷰면_404_반환() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        given(commentService.findAll(any(), any(), any(), anyInt()))
            .willThrow(new ReviewNotFoundException(reviewId));

        // when
        ResultActions result = mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString()));

        // then
        result.andExpect(status().isNotFound())
            .andDo(print());
    }

    @Test
    void 댓글_목록_조회시_잘못된_커서_형식이면_400_반환() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        String invalidCursor = "invalid-cursor-format";
        given(commentService.findAll(any(), eq(invalidCursor), any(), anyInt()))
            .willThrow(new InvalidCursorTypeException(invalidCursor, "잘못된 형식"));

        // when
        ResultActions result = mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString())
            .param("cursor", invalidCursor));

        // then
        result.andExpect(status().isBadRequest())
            .andDo(print());
    }

    @Test
    void 댓글_목록_조회시_잘못된_정렬_방향이면_400_반환() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        String invalidDirection = "INVALID";
        given(commentService.findAll(any(), any(), eq(invalidDirection), anyInt()))
            .willThrow(new IllegalArgumentException("잘못된 정렬 방향"));

        // when
        ResultActions result = mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString())
            .param("direction", invalidDirection));

        // then
        result.andExpect(status().isBadRequest())
            .andDo(print());
    }

    @Test
    void 댓글_목록_조회시_빈_결과를_반환한다() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        CursorPageResponse<CommentDto> emptyResponse = new CursorPageResponse<>(
            Collections.emptyList(),
            null,
            null,
            0,
            0L,
            false
        );

        given(commentService.findAll(any(), any(), any(), anyInt()))
            .willReturn(emptyResponse);

        // when
        ResultActions result = mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString()));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(0)))
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.hasNext").value(false))
            .andDo(print());
    }
}
