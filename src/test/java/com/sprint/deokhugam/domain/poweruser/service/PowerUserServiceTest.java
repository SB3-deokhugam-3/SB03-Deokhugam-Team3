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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("PowerUserService 테스트")
public class PowerUserServiceTest {

    @Mock
    private PowerUserRepository powerUserRepository;

    @InjectMocks
    private PowerUserService powerUserService;

    @Test
    void 활동_점수_정상_계산() {
        // given
        Double reviewScoreSum = 100.0; // 실제로는 0으로 처리됨
        Long likeCount = 50L;
        Long commentCount = 30L;
        // 현재 공식: (0.0 * 0.5) + (50 * 0.2) + (30 * 0.3) = 19.0
        Double expectedScore = (0.0 * 0.5) + (50 * 0.2) + (30 * 0.3);

        // when
        Double actualScore = PowerUserService.calculateActivityScore(
            reviewScoreSum, likeCount, commentCount);

        // then
        assertThat(actualScore).isEqualTo(expectedScore);
        assertThat(actualScore).isEqualTo(69.0);
    }

    @Test
    void 활동_점수_영값_처리() {
        // given
        Double reviewScoreSum = 0.0;
        Long likeCount = 0L;
        Long commentCount = 0L;
        Double expectedScore = 0.0;

        // when
        Double actualScore = PowerUserService.calculateActivityScore(
            reviewScoreSum, likeCount, commentCount);

        // then
        assertThat(actualScore).isEqualTo(expectedScore);
    }

    @Test
    void 활동_점수_null값_처리() {
        // given
        Double reviewScoreSum = null;
        Long likeCount = null;
        Long commentCount = null;
        Double expectedScore = 0.0; // null 값들이 0으로 처리됨

        // when
        Double actualScore = PowerUserService.calculateActivityScore(
            reviewScoreSum, likeCount, commentCount);

        // then
        assertThat(actualScore).isEqualTo(expectedScore);
    }

    @Test
    void 파워유저_저장_정상_동작() {
        // given
        List<PowerUser> powerUsers = createTestPowerUsers(3, PeriodType.DAILY);
        ArgumentCaptor<List<PowerUser>> captor = ArgumentCaptor.forClass(List.class);
        given(powerUserRepository.saveAll(captor.capture())).willReturn(powerUsers);

        // when
        powerUserService.savePowerUsers(powerUsers);

        // then
        verify(powerUserRepository, never()).deleteByPeriod(any());
        verify(powerUserRepository).saveAll(any(List.class));

        // 저장된 리스트 검증 - 간단하게 사이즈와 순위만 확인
        List<PowerUser> savedUsers = captor.getValue();
        assertThat(savedUsers).hasSize(3);

        // 모든 사용자에게 순위가 할당되었는지만 확인
        for (PowerUser user : savedUsers) {
            assertThat(user.getRank()).isGreaterThan(0L);
            assertThat(user.getRank()).isLessThanOrEqualTo(3L);
        }

        // 순위가 중복되지 않았는지 확인
        List<Long> ranks = savedUsers.stream().map(PowerUser::getRank).toList();
        assertThat(ranks).doesNotHaveDuplicates();
    }

    @Test
    void 파워유저_교체_저장_정상_동작() {
        // given
        List<PowerUser> powerUsers = createTestPowerUsers(3, PeriodType.DAILY);
        ArgumentCaptor<List<PowerUser>> captor = ArgumentCaptor.forClass(List.class);
        willDoNothing().given(powerUserRepository).deleteByPeriod(any(PeriodType.class));
        given(powerUserRepository.saveAll(captor.capture())).willReturn(powerUsers);

        // when
        powerUserService.replacePowerUsers(powerUsers);

        // then
        verify(powerUserRepository).deleteByPeriod(PeriodType.DAILY);
        verify(powerUserRepository).saveAll(any(List.class));

        // 저장된 리스트 검증 - 간단하게 사이즈와 순위만 확인
        List<PowerUser> savedUsers = captor.getValue();
        assertThat(savedUsers).hasSize(3);

        // 모든 사용자에게 유효한 순위가 할당되었는지 확인
        for (PowerUser user : savedUsers) {
            assertThat(user.getRank()).isGreaterThan(0L);
            assertThat(user.getRank()).isLessThanOrEqualTo(3L);
        }
    }

    @Test
    void 파워유저_저장_빈_리스트_처리() {
        // given
        List<PowerUser> emptyList = Collections.emptyList();

        // when
        powerUserService.savePowerUsers(emptyList);

        // then
        verify(powerUserRepository, never()).deleteByPeriod(any());
        verify(powerUserRepository, never()).saveAll(any());
    }

    @Test
    void 파워유저_교체_저장_빈_리스트_처리() {
        // given
        List<PowerUser> emptyList = Collections.emptyList();

        // when
        powerUserService.replacePowerUsers(emptyList);

        // then
        verify(powerUserRepository, never()).deleteByPeriod(any());
        verify(powerUserRepository, never()).saveAll(any());
    }

    @Test
    void 기간별_파워유저_조회_정상_동작() {
        // given
        PeriodType period = PeriodType.WEEKLY;
        int limit = 10;
        List<PowerUser> expectedUsers = createTestPowerUsers(5, period);

        given(powerUserRepository.findTopPowerUsersNByPeriod(period, limit))
            .willReturn(expectedUsers);

        // when
        List<PowerUser> actualUsers = powerUserService.getPowerUsersByPeriod(period, limit);

        // then
        assertThat(actualUsers).hasSize(5);
        assertThat(actualUsers).isEqualTo(expectedUsers);
        verify(powerUserRepository).findTopPowerUsersNByPeriod(period, limit);
    }

    @Test
    void 정렬_및_순위_기능_단순_검증() {
        // given - 점수가 다른 3명의 사용자
        List<PowerUser> powerUsers = createTestPowerUsers(3, PeriodType.WEEKLY);
        ArgumentCaptor<List<PowerUser>> captor = ArgumentCaptor.forClass(List.class);
        given(powerUserRepository.saveAll(captor.capture())).willReturn(powerUsers);

        // when
        powerUserService.savePowerUsers(powerUsers);

        // then
        List<PowerUser> savedUsers = captor.getValue();

        // 기본 검증: 모든 데이터가 저장되고 순위가 할당됨
        assertThat(savedUsers).hasSize(3);
        assertThat(savedUsers).allSatisfy(user -> {
            assertThat(user.getRank()).isBetween(1L, 3L);
            assertThat(user.getScore()).isNotNull();
        });

        // 1등이 존재하는지 확인
        boolean hasFirstPlace = savedUsers.stream()
            .anyMatch(user -> user.getRank().equals(1L));
        assertThat(hasFirstPlace).isTrue();

        // 순위 중복이 없는지 확인
        List<Long> ranks = savedUsers.stream().map(PowerUser::getRank).toList();
        assertThat(ranks).hasSize(3).doesNotHaveDuplicates();
    }

    @Test
    void 커서_기반_파워유저_조회_정상_동작() {
        // given - 마지막 페이지 시나리오 (더 이상 데이터가 없는 경우)
        PeriodType period = PeriodType.MONTHLY;
        String direction = "ASC";
        int size = 5;
        String cursor = "45"; // 뒤쪽 커서
        String after = "2024-01-15T10:30:00Z";

        // 요청한 사이즈보다 적은 데이터 반환 (마지막 페이지를 의미)
        List<PowerUser> mockUsers = createTestPowerUsersWithCreatedAt(3, period); // 5개 요청했지만 3개만 반환
        Long totalCount = 50L;

        given(powerUserRepository.findPowerUsersWithCursor(period, direction, size, cursor, after))
            .willReturn(mockUsers);
        given(powerUserRepository.countByPeriod(period))
            .willReturn(totalCount);

        // when
        CursorPageResponse<PowerUserDto> response = powerUserService
            .getPowerUsersWithCursor(period, direction, size, cursor, after);

        // then
        assertThat(response.content()).hasSize(3); // 실제로는 3개만 반환
        assertThat(response.totalElements()).isEqualTo(totalCount);
        assertThat(response.size()).isEqualTo(size);
        assertThat(response.hasNext()).isFalse(); // 요청한 수보다 적게 반환되어 다음 페이지 없음
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextAfter()).isNull();

        verify(powerUserRepository).findPowerUsersWithCursor(period, direction, size, cursor, after);
        verify(powerUserRepository).countByPeriod(period);
    }

    @Test
    void 커서_기반_파워유저_조회_다음_페이지_존재() {
        // given
        PeriodType period = PeriodType.ALL_TIME;
        String direction = "DESC";
        int size = 3;
        String cursor = null;
        String after = null;

        List<PowerUser> mockUsers = createTestPowerUsersWithCreatedAt(3, period);
        Long totalCount = 100L;

        given(powerUserRepository.findPowerUsersWithCursor(period, direction, size, cursor, after))
            .willReturn(mockUsers);
        given(powerUserRepository.countByPeriod(period))
            .willReturn(totalCount);

        // when
        CursorPageResponse<PowerUserDto> response = powerUserService
            .getPowerUsersWithCursor(period, direction, size, cursor, after);

        // then
        assertThat(response.content()).hasSize(3);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo("3");
        assertThat(response.nextAfter()).isNotNull();
        assertThat(response.totalElements()).isEqualTo(totalCount);

        verify(powerUserRepository).findPowerUsersWithCursor(period, direction, size, cursor, after);
    }

    @Test
    void 커서_기반_파워유저_조회_빈_결과_처리() {
        // given
        PeriodType period = PeriodType.DAILY;
        String direction = "ASC";
        int size = 10;
        String cursor = "100";
        String after = "2024-01-20T10:30:00Z";

        List<PowerUser> emptyList = Collections.emptyList();
        Long totalCount = 0L;

        given(powerUserRepository.findPowerUsersWithCursor(period, direction, size, cursor, after))
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

        verify(powerUserRepository).findPowerUsersWithCursor(period, direction, size, cursor, after);
        verify(powerUserRepository).countByPeriod(period);
    }

    @Test
    void DTO_변환_정상_동작() {
        // given
        User testUser = createTestUser();
        UUID testUserId = UUID.randomUUID();
        ReflectionTestUtils.setField(testUser, "id", testUserId);

        PowerUser powerUser = PowerUser.builder()
            .user(testUser)
            .period(PeriodType.WEEKLY)
            .rank(5L)
            .score(19.0) // 현재 점수 계산 로직에 맞춘 값
            .reviewScoreSum(0.0) // 리뷰 점수는 임시로 0
            .likeCount(15L)
            .commentCount(30L)
            .build();

        Instant testTime = Instant.now();
        ReflectionTestUtils.setField(powerUser, "createdAt", testTime);

        given(powerUserRepository.findPowerUsersWithCursor(
            eq(PeriodType.WEEKLY), eq("ASC"), eq(1), eq(null), eq(null)))
            .willReturn(List.of(powerUser));
        given(powerUserRepository.countByPeriod(PeriodType.WEEKLY))
            .willReturn(1L);

        // when
        CursorPageResponse<PowerUserDto> response = powerUserService
            .getPowerUsersWithCursor(PeriodType.WEEKLY, "ASC", 1, null, null);

        // then
        PowerUserDto dto = response.content().get(0);
        assertThat(dto.userId()).isEqualTo(testUserId);
        assertThat(dto.nickname()).isEqualTo(testUser.getNickname());
        assertThat(dto.period()).isEqualTo("WEEKLY");
        assertThat(dto.rank()).isEqualTo(5L);
        assertThat(dto.score()).isEqualTo(19.0);
        assertThat(dto.reviewScoreSum()).isEqualTo(0.0); // 임시로 0 반환
        assertThat(dto.likeCount()).isEqualTo(15L);
        assertThat(dto.commentCount()).isEqualTo(30L);
        assertThat(dto.createdAt()).isEqualTo(testTime);
    }

    /**
     * 테스트용 PowerUser 리스트 생성 - 단순하게 서로 다른 점수로 생성
     */
    private List<PowerUser> createTestPowerUsers(int count, PeriodType period) {
        List<PowerUser> powerUsers = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            User user = createTestUser("user" + i);
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

            // 각기 다른 점수로 생성 (i값에 따라 차이)
            long likes = 10L + (i * 10L);
            long comments = 5L + (i * 5L);
            double score = (likes * 0.2) + (comments * 0.3);

            PowerUser powerUser = PowerUser.builder()
                .user(user)
                .period(period)
                .rank(1L) // 초기값 (서비스에서 재할당됨)
                .score(score)
                .reviewScoreSum(0.0)
                .likeCount(likes)
                .commentCount(comments)
                .build();

            powerUsers.add(powerUser);
        }

        return powerUsers;
    }

    /**
     * 테스트용 PowerUser 리스트 생성 - createdAt 포함
     */
    private List<PowerUser> createTestPowerUsersWithCreatedAt(int count, PeriodType period) {
        List<PowerUser> powerUsers = new ArrayList<>();
        Instant baseTime = Instant.now();

        for (int i = 0; i < count; i++) {
            User user = createTestUser("user" + i);
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

            // 각기 다른 점수로 생성 (i값에 따라 차이)
            long likes = 10L + (i * 10L);
            long comments = 5L + (i * 5L);
            double score = (likes * 0.2) + (comments * 0.3);

            PowerUser powerUser = PowerUser.builder()
                .user(user)
                .period(period)
                .rank((long) (i + 1)) // 순위 설정
                .score(score)
                .reviewScoreSum(0.0)
                .likeCount(likes)
                .commentCount(comments)
                .build();

            // createdAt 값을 명시적으로 설정 (각각 다른 시간)
            Instant createdAt = baseTime.plusSeconds(i * 10);
            ReflectionTestUtils.setField(powerUser, "createdAt", createdAt);

            powerUsers.add(powerUser);
        }

        return powerUsers;
    }

    /**
     * 테스트용 User 생성
     */
    private User createTestUser() {
        return createTestUser("defaultUser");
    }

    /**
     * 테스트용 User 생성 ( 닉네임 지정 )
     */
    private User createTestUser(String nickname) {
        return User.builder()
            .email(nickname + "@example.com")
            .nickname(nickname + System.currentTimeMillis())
            .password("hashedPassword")
            .build();
    }
}