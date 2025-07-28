package com.sprint.deokhugam.domain.poweruser.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.config.JpaAuditingConfig;
import com.sprint.deokhugam.global.config.QueryDslConfig;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
@ActiveProfiles("test")
@DisplayName("PowerUserRepository 테스트")
public class PowerUserRepositoryTest {

    @Autowired
    private PowerUserRepository powerUserRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // given
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

        // TestEntityManager로 저장하고 flush
        testUser1 = entityManager.persistAndFlush(testUser1);
        testUser2 = entityManager.persistAndFlush(testUser2);
    }

    @Test
    void findByPeriodOrderByRankAsc_정상_조회() {
        // given
        PowerUser powerUser1 = PowerUser.builder()
            .user(testUser1)
            .period(PeriodType.DAILY)
            .rank(1L)
            .score(100.0)
            .reviewScoreSum(80.0)
            .likeCount(50L)
            .commentCount(30L)
            .build();

        PowerUser powerUser2 = PowerUser.builder()
            .user(testUser2)
            .period(PeriodType.DAILY)
            .rank(2L)
            .score(90.0)
            .reviewScoreSum(70.0)
            .likeCount(40L)
            .commentCount(25L)
            .build();

        entityManager.persistAndFlush(powerUser1);
        entityManager.persistAndFlush(powerUser2);

        // when
        List<PowerUser> result = powerUserRepository
            .findByPeriodOrderByRankAsc(PeriodType.DAILY, PageRequest.of(0, 10));

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(1).getRank()).isEqualTo(2L);
        assertThat(result.get(0).getUser().getNickname()).isEqualTo("김현기");
    }

    @Test
    void findByUserIdAndPeriod_정상_조회() {
        // given
        PowerUser powerUser = PowerUser.builder()
            .user(testUser1)
            .period(PeriodType.WEEKLY)
            .rank(5L)
            .score(85.0)
            .reviewScoreSum(65.0)
            .likeCount(35L)
            .commentCount(20L)
            .build();
        powerUserRepository.save(powerUser);

        // when
        Optional<PowerUser> result = powerUserRepository
            .findByUserIdAndPeriod(testUser1.getId(), PeriodType.WEEKLY);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getRank()).isEqualTo(5L);
        assertThat(result.get().getScore()).isEqualTo(85.0);
    }

    @Test
    void existsByUserIdAndPeriod_정상_확인() {
        // given
        PowerUser powerUser = PowerUser.builder()
            .user(testUser1)
            .period(PeriodType.MONTHLY)
            .rank(1L)
            .score(95.0)
            .reviewScoreSum(75.0)
            .likeCount(45L)
            .commentCount(25L)
            .build();
        powerUserRepository.save(powerUser);

        // when
        boolean exists1 = powerUserRepository.existsByUserIdAndPeriod(
            testUser1.getId(), PeriodType.MONTHLY);
        boolean exists2 = powerUserRepository.existsByUserIdAndPeriod(
            testUser2.getId(), PeriodType.MONTHLY);

        // then
        assertThat(exists1).isTrue();
        assertThat(exists2).isFalse();
    }

    @Test
    void countByPeriod_정상_조회() {
        // given
        List<PowerUser> powerUsers = List.of(
            PowerUser.builder().user(testUser1).period(PeriodType.ALL_TIME)
                .rank(1L).score(100.0).reviewScoreSum(80.0).likeCount(50L).commentCount(30L).build(),
            PowerUser.builder().user(testUser2).period(PeriodType.ALL_TIME)
                .rank(2L).score(95.0).reviewScoreSum(75.0).likeCount(45L).commentCount(28L).build()
        );
        powerUserRepository.saveAll(powerUsers);

        // when
        Long count = powerUserRepository.countByPeriod(PeriodType.ALL_TIME);

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void deleteByPeriod_정상_삭제() {
        // given
        List<PowerUser> powerUsers = List.of(
            PowerUser.builder().user(testUser1).period(PeriodType.DAILY)
                .rank(1L).score(100.0).reviewScoreSum(80.0).likeCount(50L).commentCount(30L).build(),
            PowerUser.builder().user(testUser2).period(PeriodType.WEEKLY)
                .rank(1L).score(90.0).reviewScoreSum(70.0).likeCount(40L).commentCount(25L).build()
        );
        powerUserRepository.saveAll(powerUsers);

        // when
        powerUserRepository.deleteByPeriod(PeriodType.DAILY);

        // then
        assertThat(powerUserRepository.countByPeriod(PeriodType.DAILY)).isEqualTo(0L);
        assertThat(powerUserRepository.countByPeriod(PeriodType.WEEKLY)).isEqualTo(1L);
    }

    @Test
    void findByPeriodAndRankBetween_순위범위_조회() {
        // given
        User testUser3 = createTestUser("test3@test.com", "유저3");
        testUser3 = entityManager.persistAndFlush(testUser3);

        List<PowerUser> powerUsers = List.of(
            createPowerUser(testUser1, PeriodType.DAILY, 1L, 100.0),
            createPowerUser(testUser2, PeriodType.DAILY, 5L, 80.0),
            createPowerUser(testUser3, PeriodType.DAILY, 10L, 60.0)
        );
        powerUserRepository.saveAll(powerUsers);

        // when
        List<PowerUser> result = powerUserRepository
            .findByPeriodAndRankBetween(PeriodType.DAILY, 1L, 5L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(1).getRank()).isEqualTo(5L);
    }

    @Test
    void findByUserIdAndPeriod_존재하지않는_사용자() {
        // when
        Optional<PowerUser> result = powerUserRepository
            .findByUserIdAndPeriod(UUID.randomUUID(), PeriodType.DAILY);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void countByPeriod_데이터없음() {
        // when
        Long count = powerUserRepository.countByPeriod(PeriodType.DAILY);

        // then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void existsByUserIdAndPeriod_존재하지않는_사용자() {
        // when
        boolean exists = powerUserRepository.existsByUserIdAndPeriod(
            UUID.randomUUID(), PeriodType.DAILY);

        // then
        assertThat(exists).isFalse();
    }

    @ParameterizedTest
    @EnumSource(PeriodType.class)
    void 모든_PeriodType에_대한_기본_조회_테스트(PeriodType period) {
        // given
        PowerUser powerUser = createPowerUser(testUser1, period, 1L, 100.0);
        powerUserRepository.save(powerUser);

        // when
        Long count = powerUserRepository.countByPeriod(period);

        // then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void findByPeriodOrderByRankAsc_페이징_없음() {
        // given
        List<PowerUser> powerUsers = List.of(
            createPowerUser(testUser1, PeriodType.WEEKLY, 1L, 100.0),
            createPowerUser(testUser2, PeriodType.WEEKLY, 2L, 90.0)
        );
        powerUserRepository.saveAll(powerUsers);

        // when
        List<PowerUser> result = powerUserRepository
            .findByPeriodOrderByRankAsc(PeriodType.WEEKLY, null);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(1).getRank()).isEqualTo(2L);
    }

    @Test
    void deleteByPeriod_해당기간_데이터없음() {
        // given - 데이터 없음

        // when & then - 예외 없이 정상 실행
        powerUserRepository.deleteByPeriod(PeriodType.DAILY);
        assertThat(powerUserRepository.countByPeriod(PeriodType.DAILY)).isEqualTo(0L);
    }

    //  헬퍼 메서드들
    private User createTestUser(String email, String nickname) {
        return User.builder()
            .email(email)
            .nickname(nickname)
            .password("password")
            .build();
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
