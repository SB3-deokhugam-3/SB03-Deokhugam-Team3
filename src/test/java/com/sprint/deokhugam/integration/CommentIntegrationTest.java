package com.sprint.deokhugam.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.deokhugam.domain.comment.entity.Comment;
import com.sprint.deokhugam.domain.comment.repository.CommentRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/test-comment-integration.sql")
@ActiveProfiles("test")
public class CommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    /* 댓글 등록 */
    @Test
    void 댓글_등록_성공_통합_테스트() throws Exception {
        //given
        CommentCreateRequest validRequest = new CommentCreateRequest(
            UUID.fromString("cea1a965-2817-4431-90e3-e5701c70d43d"),
            UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b")
            , "댓글생성테스트");

        //when
        ResultActions result = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
        );

        //then
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("댓글생성테스트"));
        // 저장 확인 (DB)
        List<Comment> comments = commentRepository.findAll();
        assertEquals(5, comments.size());
    }

    @Test
    void 댓글_등록시_conetent가_빈문자일때_실패_통합_테스트() throws Exception {
        //given
        CommentCreateRequest inValidRequest = new CommentCreateRequest(
            UUID.fromString("cea1a965-2817-4431-90e3-e5701c70d43d"),
            UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b")
            , "");

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
    void 댓글_등록시_reviewId가_없을때_실패_통합_테스트() throws Exception {
        //given
        CommentCreateRequest inValidRequest = new CommentCreateRequest(
            null,
            UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b")
            , "댓글생성테스트");

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
    void 댓글_등록시_userId가_없을때_실패_통합_테스트() throws Exception {
        //given
        CommentCreateRequest inValidRequest = new CommentCreateRequest(
            UUID.fromString("cea1a965-2817-4431-90e3-e5701c70d43d"),
            null, "댓글생성테스트");

        //when
        ResultActions result = mockMvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inValidRequest))
        );

        //then
        result.andExpect(status().is4xxClientError());
    }

    /* 댓글 세부 조회 */
    @Test
    void 댓글_세부조회_성공_통합_테스트() throws Exception {
        //given
        UUID commentId = UUID.fromString("01bd234c-d175-41b1-bd89-84995596b6f2");

        //when
        ResultActions result = mockMvc.perform(
            get("/api/comments/" + commentId)
        );

        //then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(commentId.toString()))
            .andExpect(jsonPath("$.content").value("comment1"));
    }

    @Test
    void 댓글_세부조회시_commentId가_없을때_실패_통합_테스트() throws Exception {
        //when
        ResultActions result = mockMvc.perform(
            get("/api/comments/")
        );

        //then
        result.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NO_RESOURCE_FOUND"));
    }

    /* 댓글 수정 */
    @Test
    void 댓글_수정_성공_통합_테스트() throws Exception {
        //given
        CommentUpdateRequest validRequest = new CommentUpdateRequest("댓글업데이트");
        UUID commentId = UUID.fromString("01bd234c-d175-41b1-bd89-84995596b6f2");
        UUID userId = UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b");

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
            .andExpect(jsonPath("$.content").value("댓글업데이트"));

    }

    @Test
    void 댓글_수정시_헤더에_DeokhugamRequestUserID가_없을때_실패_통합_테스트() throws Exception {
        //given
        CommentUpdateRequest validRequest = new CommentUpdateRequest("댓글업데이트");
        UUID commentId = UUID.fromString("01bd234c-d175-41b1-bd89-84995596b6f2");

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
    void 댓글_수정시_빈값일때_실패_통합_테스트() throws Exception {
        //given
        CommentUpdateRequest inValidRequest = new CommentUpdateRequest("");
        UUID commentId = UUID.fromString("01bd234c-d175-41b1-bd89-84995596b6f2");
        UUID userId = UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b");

        //when
        ResultActions result = mockMvc.perform(
            patch("/api/comments/" + commentId)
                .header("Deokhugam-Request-User-ID", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inValidRequest))
        );

        //then
        result.andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
    }

    @Test
    void 댓글_수정시_commentId가_없을때_실패_통합_테스트() throws Exception {
        //given
        CommentUpdateRequest validRequest = new CommentUpdateRequest("댓글업데이트");
        UUID notExistedcommentId = UUID.fromString("01bd234c-d175-41b1-bd89-8491236b6f2");
        UUID userId = UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b");

        //when
        ResultActions result = mockMvc.perform(
            patch("/api/comments/" + notExistedcommentId)
                .header("Deokhugam-Request-User-ID", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
        );

        //then
        result.andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.code").value("Comment_NOT_FOUND"));
    }

    /* 댓글 커서 기반 조회 */
    @Test
    void 커서_기반_댓글_목록_조회_통합_테스트() throws Exception {
        // given
        UUID reviewId = UUID.fromString("cea1a965-2817-4431-90e3-e5701c70d43d");
        String direction = "DESC";
        int limit = 2;

        // when
        ResultActions result = mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString())
            .param("direction", direction)
            .param("limit", String.valueOf(limit)));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andExpect(jsonPath("$.totalElements").value(3));
    }


    @Test
    void 댓글_목록_조회시_존재하지_않을때_실패_통합_테스트() throws Exception {
        // given
        UUID notExistedReviewId = UUID.fromString("cea1a965-2817-4431-0000-e5701c70d43d");

        // when
        ResultActions result = mockMvc.perform(get("/api/comments")
            .param("reviewId", notExistedReviewId.toString()));

        // then
        result.andExpect(status().isNotFound())
            .andDo(print());
    }

    @Test
    void 댓글_목록_조회시_잘못된_커서_형식일때_실패_통합_테스트() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        String invalidCursor = "invalid-cursor-format";

        // when
        ResultActions result = mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString())
            .param("cursor", invalidCursor));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    void 댓글_목록_조회시_잘못된_정렬_방향일때_실패_통합_테스트() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        String invalidDirection = "INVALID";

        // when
        ResultActions result = mockMvc.perform(get("/api/comments")
            .param("reviewId", reviewId.toString())
            .param("direction", invalidDirection));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    void 댓글을_논리삭제_통합_테스트() throws Exception {
        // given
        UUID commentId = UUID.fromString("01bd234c-d175-41b1-bd89-84995596b6f2");
        UUID userId = UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b");

        // when
        ResultActions result = mockMvc.perform(delete("/api/comments/{commentId}", commentId)
            .header("Deokhugam-Request-User-ID", userId.toString()));

        // then
        result.andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    void 논리삭제된_댓글을_물리삭제_통합_테스트() throws Exception {
        // given
        UUID commentId = UUID.fromString("2f499b88-fd3b-4fb7-9a33-f0976d8c76b8");
        UUID userId = UUID.fromString("04e8e411-dd9c-451e-b03e-b393557b283e");

        // When
        ResultActions result = mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
            .header("Deokhugam-Request-User-ID", userId.toString()));

        // then
        result.andDo(print())
            .andExpect(status().isNoContent());
    }

}
