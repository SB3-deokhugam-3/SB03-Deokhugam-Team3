package com.sprint.deokhugam.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.comment.dto.data.CommentDto;
import com.sprint.deokhugam.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.deokhugam.domain.comment.entity.Comment;
import com.sprint.deokhugam.domain.comment.mapper.CommentMapper;
import com.sprint.deokhugam.domain.comment.repository.CommentRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.exception.ReviewNotFoundException;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.exception.UserNotFoundException;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private BookRepository bookRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;
    private CursorPageResponse<CommentDto> mockResponse;

    private Instant createdAt;
    private Instant updatedAt;
    private User user1;
    private Book book1;
    private Review review1;

    @BeforeEach
    void 초기_설정() {
        createdAt = Instant.now();
        updatedAt = Instant.now();

        // ---------- [USER 1] ----------
        user1 = User.builder()
            .email("user1@example.com")
            .nickname("유저1")
            .password("encryptedPwd1")
            .build();
        ReflectionTestUtils.setField(user1, "id",
            UUID.fromString("36404724-4603-4cf4-8a8c-ebff46deb51b"));

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
        ReflectionTestUtils.setField(book1, "id",
            UUID.fromString("f6601c1d-c9b9-4ae1-a7aa-b4345921f4ca"));

        // ---------- [REVIEW 1] ----------
        review1 = Review.builder()
            .content("리뷰1")
            .rating(1)
            .likeCount(10L)
            .commentCount(12L)
            .isDeleted(true)
            .user(user1)
            .book(book1)
            .build();
        ReflectionTestUtils.setField(review1, "id",
            UUID.fromString("cea1a965-2817-4431-90e3-e5701c70d43d"));


    }

    @Test
    void 댓글_생성_성공시_201응답_반환() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        String content = "댓글생성테스트";
        Comment comment = new Comment(review1, user1, content);
        CommentDto expectedDto = CommentDto.builder()
            .id(commentId)
            .content(content)
            .reviewId(review1.getId())
            .userId(user1.getId())
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
        given(reviewRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(review1));
        given(userRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(user1));
        given(commentRepository.save(any())).willReturn(comment);
        given(commentMapper.toDto(comment)).willReturn(expectedDto);

        //when
        CommentDto result = commentService.create(
            new CommentCreateRequest(review1.getId(), user1.getId(), content));

        //then
        assertThat(result).isEqualTo(expectedDto);
        then(reviewRepository).should().findById(review1.getId());
        then(userRepository).should().findById(user1.getId());
        then(commentRepository).should().save(any());
        then(commentMapper).should().toDto(comment);

    }


    @Test
    void 댓글_생성_요청시_해당_리뷰가_없다면_400에러_반환() throws Exception {
        //given
        String content = "댓글생성테스트";
        given(reviewRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        //when
        Throwable thrown = catchThrowable(() -> commentService.create(
            new CommentCreateRequest(review1.getId(), user1.getId(), content)));

        //then
        assertThat(thrown).isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void 댓글_생성_요청시_해당_유저가_없다면_400에러_반환() throws Exception {
        //given
        String content = "댓글생성테스트";
        given(reviewRepository.findById(any(UUID.class))).willReturn(Optional.of(review1));
        given(userRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        //when
        Throwable thrown = catchThrowable(() -> commentService.create(
            new CommentCreateRequest(review1.getId(), user1.getId(), content)));

        //then
        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
    }

}
