package com.sprint.deokhugam.domain.poweruser.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.deokhugam.domain.poweruser.dto.PowerUserDto;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("PowerUserService 테스트")
public class PowerUserServiceTest {

    @Mock
    private PowerUserRepository powerUserRepository;

    @InjectMocks
    private PowerUserService powerUserService;

    @Test
    void calculateActivityScore_정상_계산() {
        // given
        Double reviewScoreSum = 100.0;
        Long likeCount = 50L;
        Long commentCount = 30L;
        Double expectedScore = (100.0 * 0.5) + (50 * 0.2) + (30 * 0.3); // 69.0

        // when
        Double actualScore = powerUserService.calculateActivityScore(
            reviewScoreSum, likeCount, commentCount);

        // then
        assertThat(actualScore).isEqualTo(expectedScore);
        assertThat(actualScore).isEqualTo(69.0);
    }

    @Test
    void calculateActivityScore_영값_처리() {
        // given
        Double reviewScoreSum = 0.0;
        Long likeCount = 0L;
        Long commentCount = 0L;
        Double expectedScore = 0.0;

        // when
        Double actualScore = powerUserService.calculateActivityScore(
            reviewScoreSum, likeCount, commentCount);

        // then
        assertThat(actualScore).isEqualTo(expectedScore);
    }

    @Test
    void savePowerUsers_정상_저장() {
        // given
        List<PowerUser> powerUsers = createTestPowerUsers(3, PeriodType.DAILY);
        willDoNothing().given(powerUserRepository).deleteByPeriod(any(PeriodType.class));
        given(powerUserRepository.saveAll(any(List.class))).willReturn(powerUsers);

        // when
        powerUserService.savePowerUsers(powerUsers);

        // then
        verify(powerUserRepository).deleteByPeriod(PeriodType.DAILY);
        verify(powerUserRepository).saveAll(powerUsers);

        for (int i = 0; i < powerUsers.size(); i++) {
            assertThat(powerUsers.get(i).getRank()).isEqualTo(i + 1L);
        }
    }

    @Test
    void savePowerUsers_빈_리스트_처리() {
        // given
        List<PowerUser> emptyList = Collections.emptyList();

        // when
        powerUserService.savePowerUsers(emptyList);

        // then
        verify(powerUserRepository, never()).deleteByPeriod(any());
        verify(powerUserRepository, never()).saveAll(any());
    }

    @Test
    void getPowerUsersByPeriod_정상_조회() {
        // given
        PeriodType period = PeriodType.WEEKLY;
        int limit = 10;
        List<PowerUser> expectedUsers = createTestPowerUsers(5, period);
        Pageable expectedPageable = PageRequest.of(0, limit);

        given(powerUserRepository.findByPeriodOrderByRankAsc(period, expectedPageable))
            .willReturn(expectedUsers);

        // when
        List<PowerUser> actualUsers = powerUserService.getPowerUsersByPeriod(period, limit);

        // then
        assertThat(actualUsers).hasSize(5);
        assertThat(actualUsers).isEqualTo(expectedUsers);
        verify(powerUserRepository).findByPeriodOrderByRankAsc(period, expectedPageable);
    }

    @Test
    void getPowerUsersWithCursor_정상_조회() {
        // given
        PeriodType period = PeriodType.MONTHLY;
        String direction = "ASC";
        int size = 5;
        String cursor = "3";
        String after = "2024-01-15T10:30:00Z";

        List<PowerUser> mockUsers = createTestPowerUsers(5, period);
        Long totalCount = 50L;

        given(powerUserRepository.findPowerUsersWithCursor(period, direction, size + 1, cursor, after))
            .willReturn(mockUsers);
        given(powerUserRepository.countByPeriod(period))
            .willReturn(totalCount);

        // when
        CursorPageResponse<PowerUserDto> response = powerUserService
            .getPowerUsersWithCursor(period, direction, size, cursor, after);

        // then
        assertThat(response.content()).hasSize(5);
        assertThat(response.totalElements()).isEqualTo(totalCount);
        assertThat(response.size()).isEqualTo(size);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextAfter()).isNull();

        verify(powerUserRepository).findPowerUsersWithCursor(period, direction, size + 1, cursor, after);
        verify(powerUserRepository).countByPeriod(period);
    }

    @Test
    void getPowerUsersWithCursor_다음_페이지_존재() {
        // given
        PeriodType period = PeriodType.ALL_TIME;
        String direction = "DESC";
        int size = 3;
        String cursor = null;
        String after = null;

        List<PowerUser> mockUsers = createTestPowerUsers(4, period);
        Long totalCount = 100L;

        given(powerUserRepository.findPowerUsersWithCursor(period, direction, size + 1, cursor, after))
            .willReturn(mockUsers);
        given(powerUserRepository.countByPeriod(period))
            .willReturn(totalCount);

        // when
        CursorPageResponse<PowerUserDto> response = powerUserService
            .getPowerUsersWithCursor(period, direction, size, cursor, after);

        // then
        assertThat(response.content()).hasSize(3); //
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo("3");
        assertThat(response.nextAfter()).isNotNull();
        assertThat(response.totalElements()).isEqualTo(totalCount);

        verify(powerUserRepository).findPowerUsersWithCursor(period, direction, size + 1, cursor, after);
    }

    @Test
    void getPowerUsersWithCursor_빈_결과_처리() {
        // given
        PeriodType period = PeriodType.DAILY;
        String direction = "ASC";
        int size = 10;
        String cursor = "100";
        String after = "2024-01-20T10:30:00Z";

        List<PowerUser> emptyList = Collections.emptyList();
        Long totalCount = 0L;

        given(powerUserRepository.findPowerUsersWithCursor(period, direction, size + 1, cursor, after))
            .willReturn(emptyList);
        given(powerUserRepository.countByPeriod(period))
            .willReturn(totalCount);

        // when
        CursorPageResponse<PowerUserDto> response = powerUserService
            .getPowerUsersWithCursor(period, direction, size, cursor, after);

        // then
        assertThat(response.content()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextAfter()).isNull();
        assertThat(response.totalElements()).isEqualTo(0L);

        verify(powerUserRepository).findPowerUsersWithCursor(period, direction, size + 1, cursor, after);
        verify(powerUserRepository).countByPeriod(period);
    }

    @Test
    void convertToDto_변환_성공() {
        // given
        User testUser = createTestUser();
        UUID testUserId = UUID.randomUUID();
        ReflectionTestUtils.setField(testUser, "id", testUserId);

        PowerUser powerUser = PowerUser.builder()
            .user(testUser)
            .period(PeriodType.WEEKLY)
            .rank(5L)
            .score(85.5)
            .reviewScoreSum(70.0)
            .likeCount(15L)
            .commentCount(10L)
            .build();

        Instant testTime = Instant.now();
        ReflectionTestUtils.setField(powerUser, "createdAt", testTime);
        powerUser.setRank(5L);

        // when
        given(powerUserRepository.findPowerUsersWithCursor(
            eq(PeriodType.WEEKLY), eq("ASC"), eq(2), eq(null), eq(null)))
            .willReturn(List.of(powerUser));
        given(powerUserRepository.countByPeriod(PeriodType.WEEKLY))
            .willReturn(1L);

        CursorPageResponse<PowerUserDto> response = powerUserService
            .getPowerUsersWithCursor(PeriodType.WEEKLY, "ASC", 1, null, null);

        // then
        PowerUserDto dto = response.content().get(0);
        assertThat(dto.userId()).isEqualTo(testUserId);
        assertThat(dto.nickname()).isEqualTo(testUser.getNickname());
        assertThat(dto.period()).isEqualTo("WEEKLY");
        assertThat(dto.rank()).isEqualTo(5L);
        assertThat(dto.score()).isEqualTo(85.5);
        assertThat(dto.reviewScoreSum()).isEqualTo(70.0);
        assertThat(dto.likeCount()).isEqualTo(15L);
        assertThat(dto.commentCount()).isEqualTo(10L);
        assertThat(dto.createdAt()).isEqualTo(testTime);
    }

    @Test
    void savePowerUsers_대용량_데이터_처리() {
        // given
        int largeSize = 1000;
        List<PowerUser> largePowerUsers = createTestPowerUsers(largeSize, PeriodType.ALL_TIME);

        willDoNothing().given(powerUserRepository).deleteByPeriod(PeriodType.ALL_TIME);
        given(powerUserRepository.saveAll(any(List.class))).willReturn(largePowerUsers);

        // when
        powerUserService.savePowerUsers(largePowerUsers);

        // then
        verify(powerUserRepository).deleteByPeriod(PeriodType.ALL_TIME);
        verify(powerUserRepository).saveAll(largePowerUsers);

        for (int i = 0; i < largeSize; i++) {
            assertThat(largePowerUsers.get(i).getRank()).isEqualTo(i + 1L);
        }
    }

    /**
     * 테스트용 PowerUser 리스트 생성
     */
    private List<PowerUser> createTestPowerUsers(int count, PeriodType period) {
        List<PowerUser> powerUsers = new ArrayList<>();
        Instant baseTime = Instant.now();

        for (int i = 0; i < count; i++) {
            User user = createTestUser();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID()); // ✅ 추가

            PowerUser powerUser = PowerUser.builder()
                .user(user)
                .period(period)
                .rank((long) (i + 1))
                .score(100.0 - (i * 10))
                .reviewScoreSum(80.0 - (i * 5))
                .likeCount((long) (20 - i))
                .commentCount((long) (15 - i))
                .build();

            ReflectionTestUtils.setField(powerUser, "createdAt",
                baseTime.minusSeconds(i * 60));

            powerUser.setRank((long) (i + 1));
            powerUsers.add(powerUser);
        }

        return powerUsers;
    }

    /**
     * 테스트용 User 생성 (id 없이)
     */
    private User createTestUser() {
        return User.builder()
            .email("test@example.com")
            .nickname("testUser" + System.currentTimeMillis())
            .password("hashedPassword")
            .build();
    }
}
