package com.sprint.deokhugam.domain.poweruser.batch;

import com.sprint.deokhugam.domain.poweruser.batch.processor.PowerUserProcessor;
import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.service.PowerUserService;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.enums.PeriodType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
@DisplayName("PowerUserProcessor 테스트")
class PowerUserProcessorTest {

    private PowerUserProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new PowerUserProcessor();
    }

    @Test
    void PowerUserData를_PowerUser로_정상적으로_변환() throws Exception {
        // given
        User testUser = User.builder()
            .email("test@example.com")
            .nickname("테스트유저")
            .password("password")
            .build();

        PowerUserData userData = new PowerUserData(
            testUser,
            PeriodType.DAILY,
            80.0,
            10L,
            5L
        );

        Double expectedScore = 53.0;

        // when
        try (MockedStatic<PowerUserService> mockedService = mockStatic(PowerUserService.class)) {
            mockedService.when(() -> PowerUserService.calculateActivityScore(80.0, 10L, 5L))
                .thenReturn(expectedScore);

            PowerUser result = processor.process(userData);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUser()).isEqualTo(testUser);
            assertThat(result.getPeriod()).isEqualTo(PeriodType.DAILY);
            assertThat(result.getRank()).isEqualTo(1L);
            assertThat(result.getScore()).isEqualTo(expectedScore);
            assertThat(result.getReviewScoreSum()).isEqualTo(80.0);
            assertThat(result.getLikeCount()).isEqualTo(10L);
            assertThat(result.getCommentCount()).isEqualTo(5L);
        }
    }

    @Test
    void Weekly_기간_데이터를_정상적으로_처리() throws Exception {
        // given
        User testUser = User.builder()
            .email("weekly@example.com")
            .nickname("주간유저")
            .password("password")
            .build();

        PowerUserData userData = new PowerUserData(
            testUser,
            PeriodType.WEEKLY,
            120.0,
            20L,
            10L
        );

        Double expectedScore = 77.0;

        // when
        try (MockedStatic<PowerUserService> mockedService = mockStatic(PowerUserService.class)) {
            mockedService.when(() -> PowerUserService.calculateActivityScore(120.0, 20L, 10L))
                .thenReturn(expectedScore);

            PowerUser result = processor.process(userData);

            // then
            assertThat(result.getPeriod()).isEqualTo(PeriodType.WEEKLY);
            assertThat(result.getScore()).isEqualTo(expectedScore);
            assertThat(result.getReviewScoreSum()).isEqualTo(120.0);
            assertThat(result.getLikeCount()).isEqualTo(20L);
            assertThat(result.getCommentCount()).isEqualTo(10L);
        }
    }

    @Test
    void Monthly_기간_데이터를_정상적으로_처리() throws Exception {
        // given
        User testUser = User.builder()
            .email("monthly@example.com")
            .nickname("월간유저")
            .password("password")
            .build();

        PowerUserData userData = new PowerUserData(
            testUser,
            PeriodType.MONTHLY,
            200.0,
            50L,
            25L
        );

        Double expectedScore = 117.5;

        // when
        try (MockedStatic<PowerUserService> mockedService = mockStatic(PowerUserService.class)) {
            mockedService.when(() -> PowerUserService.calculateActivityScore(200.0, 50L, 25L))
                .thenReturn(expectedScore);

            PowerUser result = processor.process(userData);

            // then
            assertThat(result.getPeriod()).isEqualTo(PeriodType.MONTHLY);
            assertThat(result.getScore()).isEqualTo(expectedScore);
            assertThat(result.getReviewScoreSum()).isEqualTo(200.0);
            assertThat(result.getLikeCount()).isEqualTo(50L);
            assertThat(result.getCommentCount()).isEqualTo(25L);
        }
    }

    @Test
    void ALL_TIME_기간_데이터를_정상적으로_처리() throws Exception {
        // given
        User testUser = User.builder()
            .email("alltime@example.com")
            .nickname("전체기간유저")
            .password("password")
            .build();

        PowerUserData userData = new PowerUserData(
            testUser,
            PeriodType.ALL_TIME,
            500.0,
            100L,
            50L
        );

        Double expectedScore = 285.0;

        // when
        try (MockedStatic<PowerUserService> mockedService = mockStatic(PowerUserService.class)) {
            mockedService.when(() -> PowerUserService.calculateActivityScore(500.0, 100L, 50L))
                .thenReturn(expectedScore);

            PowerUser result = processor.process(userData);

            // then
            assertThat(result.getPeriod()).isEqualTo(PeriodType.ALL_TIME);
            assertThat(result.getScore()).isEqualTo(expectedScore);
        }
    }

    @Test
    void 기본값이_설정된_PowerUserData를_정상적으로_처리() throws Exception {
        // given
        User testUser = User.builder()
            .email("default@example.com")
            .nickname("기본값유저")
            .password("password")
            .build();

        PowerUserData userData = new PowerUserData(
            testUser,
            PeriodType.DAILY,
            null, // 기본값 0.0으로 설정됨
            null, // 기본값 0L로 설정됨
            null  // 기본값 0L로 설정됨
        );

        Double expectedScore = 0.0;

        // when
        try (MockedStatic<PowerUserService> mockedService = mockStatic(PowerUserService.class)) {
            mockedService.when(() -> PowerUserService.calculateActivityScore(0.0, 0L, 0L))
                .thenReturn(expectedScore);

            PowerUser result = processor.process(userData);

            // then
            assertThat(result.getReviewScoreSum()).isEqualTo(0.0);
            assertThat(result.getLikeCount()).isEqualTo(0L);
            assertThat(result.getCommentCount()).isEqualTo(0L);
            assertThat(result.getScore()).isEqualTo(expectedScore);
        }
    }

    @Test
    void 점수_계산_결과가_음수인_경우_예외_발생() throws Exception {
        // given
        User testUser = User.builder()
            .email("negative@example.com")
            .nickname("음수점수유저")
            .password("password")
            .build();

        PowerUserData userData = new PowerUserData(
            testUser,
            PeriodType.DAILY,
            -10.0,
            1L,
            1L
        );

        Double negativeScore = -4.6; // 음수 점수

        // when
        try (MockedStatic<PowerUserService> mockedService = mockStatic(PowerUserService.class)) {
            mockedService.when(() -> PowerUserService.calculateActivityScore(-10.0, 1L, 1L))
                .thenReturn(negativeScore);

            // then
            assertThatThrownBy(() -> processor.process(userData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("점수는 0 이상이어야 합니다.");
        }
    }

    @Test
    void PowerUserData의_getUserSummary_메서드_호출_여부_확인() throws Exception {
        // given
        User testUser = User.builder()
            .email("summary@example.com")
            .nickname("요약테스트유저")
            .password("password")
            .build();

        PowerUserData userData = new PowerUserData(
            testUser,
            PeriodType.DAILY,
            50.0,
            5L,
            3L
        );

        Double expectedScore = 26.4; // (50.0 * 0.5) + (5 * 0.2) + (3 * 0.3) = 25 + 1 + 0.9

        // when
        try (MockedStatic<PowerUserService> mockedService = mockStatic(PowerUserService.class)) {
            mockedService.when(() -> PowerUserService.calculateActivityScore(50.0, 5L, 3L))
                .thenReturn(expectedScore);

            PowerUser result = processor.process(userData);

            // then
            assertThat(result).isNotNull();
            // getUserSummary 호출을 위해 PowerUserData 객체의 메서드들이 정상 동작하는지 확인
            String summary = userData.getUserSummary();
            assertThat(summary).contains("요약테스트유저");
            assertThat(summary).contains("50.0");
            assertThat(summary).contains("5");
            assertThat(summary).contains("3");
        }
    }

    @Test
    void PowerUserData의_getTotalActivityCount_메서드_정상_동작() throws Exception {
        // given
        User testUser = User.builder()
            .email("activity@example.com")
            .nickname("활동량테스트유저")
            .password("password")
            .build();

        PowerUserData userData = new PowerUserData(
            testUser,
            PeriodType.DAILY,
            100.0,
            15L,
            10L
        );

        // when
        long totalActivity = userData.getTotalActivityCount();

        // then
        assertThat(totalActivity).isEqualTo(25L); // 15 + 10
    }
}
