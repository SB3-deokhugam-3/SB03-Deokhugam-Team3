package com.sprint.deokhugam.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.deokhugam.domain.comment.entity.Comment;
import com.sprint.deokhugam.domain.comment.exception.CommentNotFoundException;
import com.sprint.deokhugam.domain.comment.exception.CommentNotSoftDeletedException;
import com.sprint.deokhugam.domain.comment.exception.CommentUnauthorizedAccessException;
import com.sprint.deokhugam.domain.comment.exception.InvalidCursorTypeException;
import com.sprint.deokhugam.domain.comment.mapper.CommentMapper;
import com.sprint.deokhugam.domain.comment.repository.CommentRepository;
import com.sprint.deokhugam.domain.notification.service.NotificationService;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.exception.UserNotFoundException;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 단위 테스트")
public class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommentServiceImpl commentService;

    /*테스트 초기값 설정 */
    private Instant createdAt;
    private Instant updatedAt;
    private User user1;
    private Book book1;
    private Review review1;
    private Comment comment1;
    private String content;
    private CommentDto expectedDto;

    @BeforeEach
    void 초기_설정() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        content = "댓글테스트";

        // ---------- [USER 1] ----------
        user1 = User.builder()
            .email("user1@example.com")
            .nickname("유저1")
            .password("encryptedPwd1")
            .build();
        ReflectionTestUtils.setField(user1, "id", UUID.randomUUID());

        // ---------- [BOOK 1] ----------
        book1 = Book.builder()
            .title("책1")
            .author("저자1")
            .description("설명1")
            .publisher("출판사1")
            .publishedDate(LocalDate.parse("2022-01-01"))
            .isbn("111-1111111111")
            .thumbnailUrl("https://example.com/image1.jpg")
            .reviewCount(10L)
            .rating(4.5)
            .isDeleted(false)
            .build();
        ReflectionTestUtils.setField(book1, "id", UUID.randomUUID());

        // ---------- [REVIEW 1] ----------
        review1 = Review.builder()
            .content("리뷰1")
            .rating(1)
            .likeCount(10L)
            .commentCount(12L)
            .isDeleted(false)
            .user(user1)
            .book(book1)
            .build();
        ReflectionTestUtils.setField(review1, "id", UUID.randomUUID());

        // ---------- [comment 1] ----------
        comment1 = Comment.builder()
            .user(user1)
            .review(review1)
            .content(content)
            .isDeleted(false)
            .build();
        ReflectionTestUtils.setField(comment1, "id", UUID.randomUUID());

        expectedDto = CommentDto.builder()
            .id(comment1.getId())
            .content(content)
            .reviewId(review1.getId())
            .userId(user1.getId())
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }

    @Test
    void 댓글_생성_성공시_201응답_반환() throws Exception {
        //given
        given(reviewRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(review1));
        given(userRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(user1));
        given(commentRepository.save(any())).willReturn(comment1);
        given(commentMapper.toDto(comment1)).willReturn(expectedDto);

        //when
        CommentDto result = commentService.create(
            new CommentCreateRequest(review1.getId(), user1.getId(), content));

        //then
        assertThat(result).isEqualTo(expectedDto);
        then(reviewRepository).should().findById(review1.getId());
        then(userRepository).should().findById(user1.getId());
        then(commentRepository).should().save(any());
        then(commentMapper).should().toDto(comment1);

    }

    @Test
    void 댓글_생성_요청시_해당_리뷰가_없다면_400에러_반환() {
        //given
        given(reviewRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        //when
        Throwable thrown = catchThrowable(() -> commentService.create(
            new CommentCreateRequest(review1.getId(), user1.getId(), content)));

        //then
        assertThat(thrown).isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void 댓글_생성_요청시_해당_유저가_없다면_400에러_반환() {
        //given
        given(reviewRepository.findById(any(UUID.class))).willReturn(Optional.of(review1));
        given(userRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        //when
        Throwable thrown = catchThrowable(() -> commentService.create(
            new CommentCreateRequest(review1.getId(), user1.getId(), content)));

        //then
        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void 댓글_세부정보_요청시_댓글정보_반환() throws Exception {
        //given
        given(commentRepository.findById(any())).willReturn(Optional.of(comment1));
        given(commentMapper.toDto(comment1)).willReturn(expectedDto);

        //when
        CommentDto result = commentService.findById(comment1.getId());

        //then
        assertThat(result.content()).isEqualTo(expectedDto.content());
        then(commentRepository).should().findById(comment1.getId());
        then(commentMapper).should().toDto(comment1);
    }

    @Test
    void 존재하지않은_댓글_세부정보_요청시_CommentNotFoundException에러_반환() throws Exception {
        //given
        given(commentRepository.findById(any())).willReturn(Optional.empty());

        //when
        Throwable thrown = catchThrowable(() -> commentService.findById(comment1.getId()));

        //then
        assertThat(thrown).isInstanceOf(CommentNotFoundException.class);
        then(commentRepository).should().findById(comment1.getId());
    }

    @Test
    void 댓글_수정_성공시_200응답_반환() throws Exception {
        //given
        CommentUpdateRequest validRequest = new CommentUpdateRequest(content);

        given(commentRepository.findById(any(UUID.class))).willReturn(Optional.of(comment1));
        given(commentMapper.toDto(comment1)).willReturn(expectedDto);

        //when
        CommentDto result = commentService.updateById(comment1.getId(), validRequest,
            user1.getId());

        //then
        assertThat(result).isEqualTo(expectedDto);
        then(commentRepository).should().findById(comment1.getId());
        then(commentMapper).should().toDto(comment1);
    }

    @Test
    void 댓글_수정시_존재하지않는_댓글이면_404에러_반환() throws Exception {
        //given
        CommentUpdateRequest validRequest = new CommentUpdateRequest(content);

        given(commentRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        //when
        Throwable thrown = catchThrowable(
            () -> commentService.updateById(comment1.getId(), validRequest,
                user1.getId()));

        //then
        assertThat(thrown).isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    void 댓글_수정시_유효하지않은_사용자면_404에러_반환() throws Exception {
        //given
        CommentUpdateRequest validRequest = new CommentUpdateRequest(content);

        given(commentRepository.findById(any(UUID.class))).willReturn(Optional.of(comment1));

        //when
        Throwable thrown = catchThrowable(
            () -> commentService.updateById(comment1.getId(), validRequest,
                UUID.randomUUID()));

        //then
        assertThat(thrown).isInstanceOf(CommentUnauthorizedAccessException.class);
    }

    @Test
    void 리뷰ID로_댓글_조회시_존재하지_않는_리뷰이면_예외가_발생한다() {
        // given
        UUID reviewId = UUID.randomUUID();
        given(reviewRepository.existsById(reviewId)).willReturn(false);

        // when
        Throwable thrown = catchThrowable(() -> commentService.findAll(reviewId, null, "DESC", 10));

        // then
        assertThat(thrown)
            .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void 존재하는_리뷰ID의_댓글_목록을_커서기반으로_조회할_수_있다_커서가_널일경우() {
        // given
        UUID reviewId = review1.getId();
        Instant now = Instant.now();
        int limit = 2;
        Instant createdAt1 = now.minusSeconds(30);
        Instant createdAt2 = now.minusSeconds(20);
        Comment comment1 = create(review1, user1, "댓1");
        Comment comment2 = create(review1, user1, "댓2");
        CommentDto dto1 = createDto(reviewId, "댓1", createdAt1);
        CommentDto dto2 = createDto(reviewId, "댓2", createdAt2);
        Slice<Comment> slice = new SliceImpl<>(List.of(comment1, comment2), PageRequest.of(0, 2),
            false);

        given(reviewRepository.existsById(reviewId)).willReturn(true);
        given(commentRepository.findByReviewId(eq(reviewId), any()))
            .willReturn(slice);
        given(commentMapper.toDto(comment1)).willReturn(dto1);
        given(commentMapper.toDto(comment2)).willReturn(dto2);
        given(commentRepository.countByReviewId(reviewId)).willReturn(10L);

        // when
        CursorPageResponse<CommentDto> result =
            commentService.findAll(reviewId, null, "DESC", limit);

        // then
        assertThat(result.content()).containsExactly(dto1, dto2);
        assertThat(result.totalElements()).isEqualTo(10L);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void 존재하는_리뷰ID의_댓글_목록을_커서기반으로_조회할_수_있다() {
        // given
        UUID reviewId = review1.getId();
        Instant now = Instant.now();
        String cursor = now.toString();
        int limit = 2;
        Instant createdAt1 = now.minusSeconds(30);
        Instant createdAt2 = now.minusSeconds(20);
        Comment comment1 = create(review1, user1, "댓1");
        Comment comment2 = create(review1, user1, "댓2");
        CommentDto dto1 = createDto(reviewId, "댓1", createdAt1);
        CommentDto dto2 = createDto(reviewId, "댓2", createdAt2);
        Slice<Comment> slice = new SliceImpl<>(List.of(comment1, comment2), PageRequest.of(0, 2),
            true);

        given(reviewRepository.existsById(reviewId)).willReturn(true);
        given(commentRepository.findByReviewIdAndCreatedAtLessThan(eq(reviewId), any(), any()))
            .willReturn(slice);
        given(commentMapper.toDto(comment1)).willReturn(dto1);
        given(commentMapper.toDto(comment2)).willReturn(dto2);
        given(commentRepository.countByReviewId(reviewId)).willReturn(10L);

        // when
        CursorPageResponse<CommentDto> result =
            commentService.findAll(reviewId, cursor, "DESC", limit);

        // then
        assertThat(result.content()).containsExactly(dto1, dto2);
        assertThat(result.totalElements()).isEqualTo(10L);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(dto2.createdAt().toString());
    }

    @Test
    void 댓글_전체_목록_조회시_커서타입이_맞지않으면_InvalidCursorTypeException를_던진다() {
        // given
        UUID reviewId = review1.getId();
        String invalidCursor = "invalid-cursor-format";
        given(reviewRepository.existsById(reviewId)).willReturn(true);

        // when
        Throwable thrown = catchThrowable(
            () -> commentService.findAll(reviewId, invalidCursor, "DESC", 10));

        // then
        assertThat(thrown)
            .isInstanceOf(InvalidCursorTypeException.class);
    }

    @Test
    @DisplayName("댓글을_논리삭제하면_isDeleted가_true가_된다")
    void 댓글을_논리삭제하면_isDeleted가_true가_된다() {
        // Given
        UUID commentId = comment1.getId();
        given(commentRepository.findById(commentId))
            .willReturn(Optional.of(comment1));

        // When
        commentService.softDelete(commentId, user1.getId());

        // Then
        verify(commentRepository).findById(comment1.getId());
    }

    @Test
    @DisplayName("존재하지_않는_댓글_논리삭제시_예외가_발생한다")
    void 존재하지_않는_댓글_논리삭제시_예외가_발생한다() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        given(commentRepository.findById(nonExistentId))
            .willReturn(Optional.empty());

        // When
        Throwable result = catchThrowable(() ->
            commentService.softDelete(nonExistentId, user1.getId()));

        // Then
        assertThat(result).isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("권한없는_사용자가_댓글_논리삭제시_예외가_발생한다")
    void 권한없는_사용자가_댓글_논리삭제시_예외가_발생한다() {
        // Given
        UUID reviewId = UUID.randomUUID();
        UUID unauthorizedUserId = UUID.randomUUID();
        given(commentRepository.findById(reviewId))
            .willReturn(Optional.of(comment1));

        // When
        Throwable result = catchThrowable(() ->
            commentService.softDelete(reviewId, unauthorizedUserId));

        // Then
        assertThat(result).isInstanceOf(CommentUnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("논리삭제된_댓글을_물리삭제하면_DB에서_완전히_삭제된다")
    void 논리삭제된_댓글을_물리삭제하면_DB에서_완전히_삭제된다() {
        // Given
        UUID reviewId = UUID.randomUUID();
        Comment softDeletedComment = createSoftDeleted(review1, user1, "댓1");

        given(commentRepository.findByIdIncludingDeleted(reviewId))
            .willReturn(Optional.of(softDeletedComment));

        // When
        commentService.hardDelete(reviewId, user1.getId());

        // Then
        verify(commentRepository).delete(softDeletedComment);
    }

    @Test
    @DisplayName("존재하지_않는_댓글_물리삭제시_예외가_발생한다")
    void 존재하지_않는_댓글_물리삭제시_예외가_발생한다() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        given(commentRepository.findByIdIncludingDeleted(nonExistentId))
            .willReturn(Optional.empty());

        // When
        Throwable result = catchThrowable(() ->
            commentService.hardDelete(nonExistentId, user1.getId()));

        // Then
        assertThat(result).isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    @DisplayName("논리삭제되지_않은_댓글_물리삭제시_예외가_발생한다")
    void 논리삭제되지_않은_댓글_물리삭제시_예외가_발생한다() {
        // Given
        given(commentRepository.findByIdIncludingDeleted(comment1.getId()))
            .willReturn(Optional.of(comment1));

        // When
        Throwable result = catchThrowable(() ->
            commentService.hardDelete(comment1.getId(), user1.getId()));

        // Then
        assertThat(result).isInstanceOf(CommentNotSoftDeletedException.class);
    }

    @Test
    @DisplayName("권한없는_사용자가_댓글_물리삭제시_예외가_발생한다")
    void 권한없는_사용자가_댓글_물리삭제시_예외가_발생한다() {
        // Given
        UUID unauthorizedUserId = UUID.randomUUID();
        Comment softDeletedComment = createSoftDeleted(review1, user1, "댓1");
        given(commentRepository.findByIdIncludingDeleted(softDeletedComment.getId()))
            .willReturn(Optional.of(softDeletedComment));

        // When
        Throwable result = catchThrowable(() ->
            commentService.hardDelete(softDeletedComment.getId(), unauthorizedUserId));

        // Then
        assertThat(result).isInstanceOf(CommentUnauthorizedAccessException.class);
    }

    private CommentDto createDto(UUID reviewId, String content, Instant createdAt) {
        return CommentDto.builder()
            .id(UUID.randomUUID())
            .reviewId(reviewId)
            .userId(user1.getId())
            .userNickname(user1.getNickname())
            .content(content)
            .createdAt(createdAt)
            .updatedAt(createdAt.plusSeconds(10))
            .build();
    }

    private Comment create(Review review, User user, String content) {
        return new Comment(review, user, content);
    }

    private static Comment createSoftDeleted(Review review, User user, String content) {
        Comment comment = new Comment(review, user, content);
        comment.softDelete();
        ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
        return comment;
    }

}
