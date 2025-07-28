package com.sprint.deokhugam.domain.poweruser.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import com.sprint.deokhugam.domain.popularreview.service.PopularReviewService;
import com.sprint.deokhugam.domain.poweruser.batch.processor.PowerUserProcessor;
import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.service.PowerUserService;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.enums.PeriodType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PowerUserProcessor 테스트")
class PowerUserProcessorTest {

    @Mock
    private PopularReviewService popularReviewService;

    @InjectMocks
    private PowerUserProcessor processor;

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
    void null_데이터_처리() throws Exception {
        // when
        PowerUser result = processor.process(null);

        // then
        assertThat(result).isNull();
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
            0.0, // 기본값
            0L,  // 기본값
            0L   // 기본값
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
}