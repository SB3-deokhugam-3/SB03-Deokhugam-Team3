package com.sprint.deokhugam.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.comment.entity.Comment;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.reviewlike.entity.ReviewLike;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.config.JpaAuditingConfig;
import com.sprint.deokhugam.global.config.QueryDslConfig;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
@ActiveProfiles("test")
@DisplayName("PowerUserRepository 통합 테스트")
public class PowerUserRepositoryIntegrationTest {

    @Autowired
    private PowerUserRepository powerUserRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser1;
    private User testUser2;
    private Book testBook1;
    private Book testBook2;
    private Review testReview1;
    private Review testReview2;

    @BeforeEach
    void setUp() {
        // 사용자 생성
        testUser1 = User.builder()
            .email("test1@example.com")
            .nickname("김현기")
            .password("password")
            .build();

        testUser2 = User.builder()
            .email("test2@example.com")
            .nickname("아이스티")
            .password("password")
            .build();

        testUser1 = entityManager.persistAndFlush(testUser1);
        testUser2 = entityManager.persistAndFlush(testUser2);

        // 도서 생성 - publishedDate 사용
        testBook1 = Book.builder()
            .title("테스트 도서1")
            .author("작가1")
            .description("설명1")
            .publisher("출판사1")
            .publishedDate(LocalDate.of(2024, 1, 1))  // publishDate → publishedDate
            .isbn("1111111111111")
            .thumbnailUrl("http://example.com/1.jpg")
            .rating(4.5)
            .reviewCount(10L)
            .build();

        testBook2 = Book.builder()
            .title("테스트 도서2")
            .author("작가2")
            .description("설명2")
            .publisher("출판사2")
            .publishedDate(LocalDate.of(2024, 2, 1))  // publishDate → publishedDate
            .isbn("2222222222222")
            .thumbnailUrl("http://example.com/2.jpg")
            .rating(4.0)
            .reviewCount(5L)
            .build();

        testBook1 = entityManager.persistAndFlush(testBook1);
        testBook2 = entityManager.persistAndFlush(testBook2);

        // 리뷰 생성 - isDeleted 필드 명시적으로 설정
        testReview1 = Review.builder()
            .user(testUser1)
            .book(testBook1)
            .rating(5)
            .content("노잼 책")
            .likeCount(0L)
            .commentCount(0L)
            .isDeleted(false)
            .build();

        testReview2 = Review.builder()
            .user(testUser2)
            .book(testBook2)
            .rating(4)
            .content("좋은 책")
            .likeCount(0L)
            .commentCount(0L)
            .isDeleted(false)
            .build();

        testReview1 = entityManager.persistAndFlush(testReview1);
        testReview2 = entityManager.persistAndFlush(testReview2);
    }

    @Test
    void calculateAndCreatePowerUsers_복합_집계_쿼리_통합_테스트() {
        // given
        createTestReviewLikes();
        createTestComments();

        // when
        List<PowerUser> result = powerUserRepository
            .calculateAndCreatePowerUsers(PeriodType.ALL_TIME, null, null);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getPeriod()).isEqualTo(PeriodType.ALL_TIME);
        assertThat(result.get(0).getRank()).isEqualTo(1L);

        // 활동 점수 계산 검증: ( 리뷰점수*0.5 ) + ( 좋아요*0.2 ) + ( 댓글*0.3 )
        PowerUser topUser = result.get(0);
        Double expectedScore = (topUser.getReviewScoreSum() * 0.5) +
            (topUser.getLikeCount() * 0.2) +
            (topUser.getCommentCount() * 0.3);
        assertThat(topUser.getScore()).isEqualTo(expectedScore);
    }

    @Test
    void calculateAndCreatePowerUsers_기간_필터링_통합_테스트() {
        // given
        createTestReviewLikes();
        createTestComments();

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(1);

        // when
        List<PowerUser> result = powerUserRepository
            .calculateAndCreatePowerUsers(
                PeriodType.DAILY,
                startTime.toInstant(ZoneOffset.UTC),
                endTime.toInstant(ZoneOffset.UTC)
            );

        // then
        assertThat(result).isNotNull();
        result.forEach(powerUser -> {
            assertThat(powerUser.getPeriod()).isEqualTo(PeriodType.DAILY);
            assertThat(powerUser.getRank()).isPositive();
        });
    }

    @Test
    void calculateAndCreatePowerUsers_리뷰_없는_사용자_제외() {
        // given
        User userWithoutReview = User.builder()
            .email("noreview@example.com")
            .nickname("리뷰없음")
            .password("password")
            .build();
        entityManager.persistAndFlush(userWithoutReview);

        // when
        List<PowerUser> result = powerUserRepository
            .calculateAndCreatePowerUsers(PeriodType.ALL_TIME, null, null);

        // then
        assertThat(result).allMatch(p ->
            p.getUser().getId().equals(testUser1.getId()) ||
                p.getUser().getId().equals(testUser2.getId())
        );
        // 리뷰 없는 사용자는 제외
        assertThat(result).noneMatch(p -> p.getUser().getId().equals(userWithoutReview.getId()));
    }

    @Test
    void recalculateRank_점수기준_순위재계산_통합_테스트() {
        // given
        List<PowerUser> initialPowerUsers = List.of(
            PowerUser.builder()
                .user(testUser1)
                .period(PeriodType.WEEKLY)
                .rank(2L)
                .score(100.0)
                .reviewScoreSum(80.0)
                .likeCount(50L)
                .commentCount(30L)
                .build(),
            PowerUser.builder()
                .user(testUser2)
                .period(PeriodType.WEEKLY)
                .rank(1L)
                .score(50.0)
                .reviewScoreSum(40.0)
                .likeCount(20L)
                .commentCount(15L)
                .build()
        );
        powerUserRepository.saveAll(initialPowerUsers);
        entityManager.flush();

        // when
        powerUserRepository.recalculateRank(PeriodType.WEEKLY);
        entityManager.flush();
        entityManager.clear();

        // then
        List<PowerUser> result = powerUserRepository
            .findByPeriodOrderByRankAsc(PeriodType.WEEKLY, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getScore()).isGreaterThan(result.get(1).getScore());
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(1).getRank()).isEqualTo(2L);
        assertThat(result.get(0).getUser().getId()).isEqualTo(testUser1.getId());
    }

    @Test
    void findTopPowerUsersNByPeriod_fetchJoin_성능_최적화_통합_테스트() {
        // given
        List<PowerUser> powerUsers = List.of(
            createPowerUser(testUser1, PeriodType.MONTHLY, 1L, 100.0),
            createPowerUser(testUser2, PeriodType.MONTHLY, 2L, 90.0)
        );
        powerUserRepository.saveAll(powerUsers);
        entityManager.flush();
        entityManager.clear();

        // when
        List<PowerUser> result = powerUserRepository
            .findTopPowerUsersNByPeriod(PeriodType.MONTHLY, 2);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(1).getRank()).isEqualTo(2L);

        // fetchJoin으로 User가 이미 로드되어 있어야 함
        assertThat(result.get(0).getUser().getNickname()).isEqualTo("김현기");
        assertThat(result.get(1).getUser().getNickname()).isEqualTo("아이스티");
    }

    @Test
    void batchUpsertPowerUsers_대용량_배치_처리_통합_테스트() {
        // given
        List<PowerUser> powerUsers = List.of(
            createPowerUser(testUser1, PeriodType.ALL_TIME, 1L, 100.0),
            createPowerUser(testUser2, PeriodType.ALL_TIME, 2L, 90.0)
        );

        // when
        powerUserRepository.batchUpsertPowerUsers(powerUsers);
        entityManager.flush();
        entityManager.clear();

        // then
        List<PowerUser> result = powerUserRepository
            .findByPeriodOrderByRankAsc(PeriodType.ALL_TIME, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getScore()).isEqualTo(100.0);
        assertThat(result.get(1).getScore()).isEqualTo(90.0);
    }

    @Test
    void findPowerUsersWithCursor_커서기반페이징_복합조건_통합_테스트() {
        // given
        List<PowerUser> powerUsers = List.of(
            createPowerUser(testUser1, PeriodType.DAILY, 1L, 100.0),
            createPowerUser(testUser2, PeriodType.DAILY, 2L, 90.0),
            createPowerUser(testUser1, PeriodType.DAILY, 3L, 80.0)
        );
        powerUserRepository.saveAll(powerUsers);

        // when
        List<PowerUser> result = powerUserRepository
            .findPowerUsersWithCursor(PeriodType.DAILY, "ASC", 2, "2", null);

        // then
        assertThat(result).hasSize(1); // rank > 2인 데이터만
        assertThat(result.get(0).getRank()).isEqualTo(3L);

        // DESC 테스트
        List<PowerUser> descResult = powerUserRepository
            .findPowerUsersWithCursor(PeriodType.DAILY, "DESC", 2, "3", null);

        assertThat(descResult).hasSize(2);
        assertThat(descResult.get(0).getRank()).isEqualTo(2L);
        assertThat(descResult.get(1).getRank()).isEqualTo(1L);
    }

    @Test
    void findPowerUsersWithCursor_시간기반필터링_통합_테스트() {
        // given
        PowerUser powerUser = createPowerUser(testUser1, PeriodType.WEEKLY, 1L, 100.0);
        powerUserRepository.save(powerUser);
        entityManager.flush();

        // 미래 시간으로 after 설정
        String futureTime = Instant.now().plusSeconds(3600).toString();

        // when - 미래 시간 이후의 데이터 조회 (ASC)
        List<PowerUser> result = powerUserRepository
            .findPowerUsersWithCursor(PeriodType.WEEKLY, "ASC", 10, null, futureTime);

        // then - 현재 데이터는 미래 시간 이전이므로 조회되지 않아야 함
        assertThat(result).isEmpty();

        // 과거 시간으로 설정하면 조회되어야 함
        String pastTime = Instant.now().minusSeconds(3600).toString();
        List<PowerUser> pastResult = powerUserRepository
            .findPowerUsersWithCursor(PeriodType.WEEKLY, "ASC", 10, null, pastTime);

        assertThat(pastResult).hasSize(1);
    }

    @Test
    void findPowerUsersWithCursor_잘못된_커서_무시() {
        // given
        List<PowerUser> powerUsers = List.of(
            createPowerUser(testUser1, PeriodType.MONTHLY, 1L, 100.0),
            createPowerUser(testUser2, PeriodType.MONTHLY, 2L, 90.0)
        );
        powerUserRepository.saveAll(powerUsers);

        // when - 잘못된 커서 형식
        List<PowerUser> result = powerUserRepository
            .findPowerUsersWithCursor(PeriodType.MONTHLY, "ASC", 10, "invalid_cursor", null);

        // then - 커서가 무시되고 전체 데이터 조회
        assertThat(result).hasSize(2);
    }

    @Test
    void findPowerUserHistoryByUserId_사용자_이력_조회_정렬_확인() {
        // given
        List<PowerUser> powerUsers = List.of(
            createPowerUser(testUser1, PeriodType.DAILY, 1L, 100.0),
            createPowerUser(testUser1, PeriodType.WEEKLY, 2L, 95.0),
            createPowerUser(testUser1, PeriodType.MONTHLY, 3L, 90.0),
            createPowerUser(testUser2, PeriodType.DAILY, 4L, 85.0) // 다른 사용자
        );
        powerUserRepository.saveAll(powerUsers);

        // when
        List<PowerUser> result = powerUserRepository
            .findPowerUserHistoryByUserId(testUser1.getId());

        // then
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(p -> p.getUser().getId().equals(testUser1.getId()));

        // period 기준으로 정렬
        assertThat(result.get(0).getPeriod()).isEqualTo(PeriodType.DAILY);
    }

    private void createTestReviewLikes() {
        // testReview1에 좋아요 3개
        for (int i = 0; i < 3; i++) {
            User likeUser = User.builder()
                .email("like" + i + "@example.com")
                .nickname("좋아요" + i)
                .password("password")
                .build();
            likeUser = entityManager.persistAndFlush(likeUser);

            ReviewLike reviewLike = new ReviewLike(testReview1, likeUser);
            entityManager.persistAndFlush(reviewLike);
        }

        // testReview2에 좋아요 1개
        User likeUser = User.builder()
            .email("like_single@example.com")
            .nickname("좋아요단일")
            .password("password")
            .build();
        likeUser = entityManager.persistAndFlush(likeUser);

        // ReviewLike 생성 - 올바른 매개변수 순서: (Review, User)
        ReviewLike reviewLike = new ReviewLike(testReview2, likeUser);
        entityManager.persistAndFlush(reviewLike);
    }

    private void createTestComments() {
        // testReview1에 댓글 2개
        for (int i = 0; i < 2; i++) {
            Comment comment = Comment.builder()
                .user(testUser2)
                .review(testReview1)
                .content("댓글 내용 " + i)
                .build();
            entityManager.persistAndFlush(comment);
        }

        // testReview2에 댓글 1개
        Comment comment = Comment.builder()
            .user(testUser1)
            .review(testReview2)
            .content("좋은 리뷰네요!")
            .build();
        entityManager.persistAndFlush(comment);
    }

    private PowerUser createPowerUser(User user, PeriodType period, Long rank, Double score) {
        return PowerUser.builder()
            .user(user)
            .period(period)
            .rank(rank)
            .score(score)
            .reviewScoreSum(score * 0.8)
            .likeCount(50L)
            .commentCount(30L)
            .build();
    }
}