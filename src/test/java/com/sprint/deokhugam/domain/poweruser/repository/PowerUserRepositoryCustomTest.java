package com.sprint.deokhugam.domain.poweruser.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.config.JpaAuditingConfig;
import com.sprint.deokhugam.global.config.QueryDslConfig;
import com.sprint.deokhugam.global.enums.PeriodType;
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
@DisplayName("PowerUserRepositoryCustom 테스트")
public class PowerUserRepositoryCustomTest {

    @Autowired
    private PowerUserRepository powerUserRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void findPowerUsersWithCursor_ASC_정렬_커서_없음() {
        // given
        List<PowerUser> powerUsers = List.of(
            createPowerUser(testUser1, PeriodType.DAILY, 1L, 100.0),
            createPowerUser(testUser2, PeriodType.DAILY, 2L, 90.0)
        );
        powerUserRepository.saveAll(powerUsers);
        entityManager.flush();

        // when
        List<PowerUser> result = powerUserRepository
            .findPowerUsersWithCursor(PeriodType.DAILY, "ASC", 10, null, null);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(1).getRank()).isEqualTo(2L);
    }

    @Test
    void findPowerUsersWithCursor_DESC_정렬() {
        // given
        List<PowerUser> powerUsers = List.of(
            createPowerUser(testUser1, PeriodType.WEEKLY, 1L, 100.0),
            createPowerUser(testUser2, PeriodType.WEEKLY, 2L, 90.0)
        );
        powerUserRepository.saveAll(powerUsers);
        entityManager.flush();

        // when
        List<PowerUser> result = powerUserRepository
            .findPowerUsersWithCursor(PeriodType.WEEKLY, "DESC", 10, null, null);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isEqualTo(2L); // DESC 정렬
        assertThat(result.get(1).getRank()).isEqualTo(1L);
    }

    @Test
    void findTopPowerUsersNByPeriod_상위_N명_조회() {
        // given
        List<PowerUser> powerUsers = List.of(
            createPowerUser(testUser1, PeriodType.MONTHLY, 1L, 100.0),
            createPowerUser(testUser2, PeriodType.MONTHLY, 2L, 90.0)
        );
        powerUserRepository.saveAll(powerUsers);

        // when
        List<PowerUser> result = powerUserRepository
            .findTopPowerUsersNByPeriod(PeriodType.MONTHLY, 1);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(0).getScore()).isEqualTo(100.0);
    }

    @Test
    void findPowerUserHistoryByUserId_사용자_이력_조회() {
        // given
        List<PowerUser> powerUsers = List.of(
            createPowerUser(testUser1, PeriodType.DAILY, 1L, 100.0),
            createPowerUser(testUser1, PeriodType.WEEKLY, 2L, 95.0),
            createPowerUser(testUser2, PeriodType.DAILY, 3L, 85.0)
        );
        powerUserRepository.saveAll(powerUsers);

        // when
        List<PowerUser> result = powerUserRepository
            .findPowerUserHistoryByUserId(testUser1.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.getUser().getId().equals(testUser1.getId()));
    }

    @Test
    void recalculateRank_순위_재계산() {
        // given
        List<PowerUser> powerUsers = List.of(
            createPowerUser(testUser1, PeriodType.ALL_TIME, 2L, 100.0),
            createPowerUser(testUser2, PeriodType.ALL_TIME, 1L, 90.0)
        );
        powerUserRepository.saveAll(powerUsers);
        entityManager.flush();

        // when
        powerUserRepository.recalculateRank(PeriodType.ALL_TIME);
        entityManager.flush();
        entityManager.clear();

        // then
        List<PowerUser> result = powerUserRepository
            .findByPeriodOrderByRankAsc(PeriodType.ALL_TIME, null);

        // 점수가 높은 user1이 1위, user2가 2위가 되어야 함
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUser().getId()).isEqualTo(testUser1.getId());
        assertThat(result.get(0).getRank()).isEqualTo(1L);
        assertThat(result.get(0).getScore()).isEqualTo(100.0);
        assertThat(result.get(1).getUser().getId()).isEqualTo(testUser2.getId());
        assertThat(result.get(1).getRank()).isEqualTo(2L);
        assertThat(result.get(1).getScore()).isEqualTo(90.0);
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

