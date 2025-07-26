package com.sprint.deokhugam.domain.comment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.comment.entity.Comment;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.config.JpaAuditingConfig;
import com.sprint.deokhugam.global.config.QueryDslConfig;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
@ActiveProfiles("test")
@DisplayName("CommentRepository 단위 테스트")
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CommentRepository commentRepository;

    private User user1;
    private Book book1;
    private Review review1;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
            .email("test@example.com")
            .nickname("테스트유저")
            .password("password123!")
            .build();
        em.persistAndFlush(user1);

        book1 = Book.builder()
            .title("테스트 책")
            .author("테스트 작가")
            .description("테스트 설명")
            .publisher("테스트 출판사")
            .publishedDate(LocalDate.now())
            .isbn("1234567890123")
            .rating(0.0)      // 기본값 설정
            .reviewCount(0L)  // 기본값 설정
            .build();
        em.persistAndFlush(book1);

        review1 = Review.builder()
            .book(book1)
            .user(user1)
            .content("테스트 리뷰")
            .rating(4)
            .likeCount(0L)
            .commentCount(0L)
            .isDeleted(false)
            .build();
        em.persistAndFlush(review1);

        em.flush();
        em.clear();
    }

    @Test
    void countByReviewId_정상_카운트() {
        // given
        Instant baseTime = Instant.now();
        Comment comment1 = createCommentWithTime("댓1", baseTime.minusSeconds(300));
        Comment comment2 = createCommentWithTime("댓2", baseTime.minusSeconds(180));
        em.persistAndFlush(comment1);
        em.persistAndFlush(comment2);

        // when
        Long count = commentRepository.countByReviewId(review1.getId());

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void countByReviewId_존재하지_않는_리뷰면_0_반환() {
        // given
        UUID nonExistentReviewId = UUID.randomUUID();

        // when
        Long count = commentRepository.countByReviewId(nonExistentReviewId);

        // then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void 기본_조회에서_논리삭제된_댓글은_조회되지_않는다() {
        // Given
        Comment comment = create("삭제된 댓글");
        comment.softDelete();
        em.persist(comment);
        em.flush();
        em.clear();

        // When
        Optional<Comment> result = commentRepository.findById(comment.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void 논리삭제된_댓글을_포함해서도_조회할_수_있다() {
        // Given
        Comment comment = create("삭제된 댓글");
        comment.softDelete();
        em.persist(comment);
        em.flush();
        em.clear();

        // When
        Optional<Comment> result = commentRepository.findByIdIncludingDeleted(comment.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getIsDeleted()).isTrue();
    }

    @Test
    void  ASC_정렬로_댓글을_조회하면_오래된_순으로_반환된다() {
        // given
        Instant baseTime = Instant.now();
        Comment comment1 = createCommentWithTime("댓1", baseTime.minusSeconds(300));
        Comment comment2 = createCommentWithTime("댓2", baseTime.minusSeconds(180));
        Comment comment3 = createCommentWithTime("댓3", baseTime.minusSeconds(50));
        em.persistAndFlush(comment1);
        em.persistAndFlush(comment2);
        em.persistAndFlush(comment3);

        // when
        List<Comment> result = commentRepository.fetchComments(
            review1.getId(),
            null,
            null,
            Sort.Direction.ASC,
            10
        );

        // then
        assertThat(result).extracting(Comment::getContent)
            .containsExactly("댓1", "댓2", "댓3");
    }

    @Test
    void DESC_정렬로_댓글을_조회하면_최신_순으로_반환된다() {
        // given
        Instant baseTime = Instant.now();
        Comment comment1 = createCommentWithTime("댓1", baseTime.minusSeconds(300));
        Comment comment2 = createCommentWithTime("댓2", baseTime.minusSeconds(180));
        Comment comment3 = createCommentWithTime("댓3", baseTime.minusSeconds(50));
        em.persistAndFlush(comment1);
        em.persistAndFlush(comment2);
        em.persistAndFlush(comment3);

        // when
        List<Comment> result = commentRepository.fetchComments(
            review1.getId(),
            null,
            null,
            Sort.Direction.DESC,
            10
        );

        // then
        assertThat(result).extracting(Comment::getContent)
            .containsExactly("댓3", "댓2", "댓1");
    }

    private Comment createCommentWithTime(String content, Instant createdAt) {
        Comment comment = new Comment(review1, user1, content);
        ReflectionTestUtils.setField(comment, "createdAt", createdAt);
        ReflectionTestUtils.setField(comment, "updatedAt", createdAt);
        ReflectionTestUtils.setField(comment, "isDeleted", false);
        return comment;
    }

    private Comment create(String content) {
        Comment comment = new Comment(review1, user1, content);
        return comment;
    }
}
