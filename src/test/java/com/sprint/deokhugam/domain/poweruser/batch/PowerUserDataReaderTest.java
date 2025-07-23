package com.sprint.deokhugam.domain.poweruser.batch;

import com.sprint.deokhugam.domain.poweruser.batch.reader.PowerUserDataReader;
import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.enums.PeriodType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PowerUserDateReader 테스트")
class PowerUserDataReaderTest {

    @Mock
    private PowerUserRepository powerUserRepository;

    private PowerUserDataReader reader;

    @BeforeEach
    void setUp() {
        reader = new PowerUserDataReader(powerUserRepository);
    }

    @Test
    void Daily_기간으로_데이터를_정상적으로_읽음() throws Exception {
        // given
        List<PowerUserData> mockData = createMockPowerUserData(PeriodType.DAILY);
        when(powerUserRepository.findUserActivityData(eq(PeriodType.DAILY), any(Instant.class), any(Instant.class)))
            .thenReturn(mockData);

        reader.setPeriod(PeriodType.DAILY);

        // when
        PowerUserData firstData = reader.read();
        PowerUserData secondData = reader.read();
        PowerUserData thirdData = reader.read();

        // then
        assertThat(firstData).isNotNull();
        assertThat(firstData.user().getNickname()).isEqualTo("테스트유저1");
        assertThat(firstData.period()).isEqualTo(PeriodType.DAILY);
        assertThat(firstData.reviewScoreSum()).isEqualTo(80.0);
        assertThat(firstData.likeCount()).isEqualTo(10L);
        assertThat(firstData.commentCount()).isEqualTo(5L);

        assertThat(secondData).isNotNull();
        assertThat(secondData.user().getNickname()).isEqualTo("테스트유저2");

        assertThat(thirdData).isNull(); // 데이터 끝
    }

    @Test
    void Weekly_기간으로_데이터를_정상적으로_읽음() throws Exception {
        // given
        List<PowerUserData> mockData = createMockPowerUserData(PeriodType.WEEKLY);
        when(powerUserRepository.findUserActivityData(eq(PeriodType.WEEKLY), any(Instant.class), any(Instant.class)))
            .thenReturn(mockData);

        reader.setPeriod(PeriodType.WEEKLY);

        // when
        PowerUserData result = reader.read();

        // then
        assertThat(result).isNotNull();
        assertThat(result.period()).isEqualTo(PeriodType.WEEKLY);
    }

    @Test
    void Monthly_기간으로_데이터를_정상적으로_읽음() throws Exception {
        // given
        List<PowerUserData> mockData = createMockPowerUserData(PeriodType.MONTHLY);
        when(powerUserRepository.findUserActivityData(eq(PeriodType.MONTHLY), any(Instant.class), any(Instant.class)))
            .thenReturn(mockData);

        reader.setPeriod(PeriodType.MONTHLY);

        // when
        PowerUserData result = reader.read();

        // then
        assertThat(result).isNotNull();
        assertThat(result.period()).isEqualTo(PeriodType.MONTHLY);
    }

    @Test
    void ALL_TIME_기간으로_데이터를_정상적으로_읽음() throws Exception {
        // given
        List<PowerUserData> mockData = createMockPowerUserData(PeriodType.ALL_TIME);
        when(powerUserRepository.findUserActivityData(eq(PeriodType.ALL_TIME), isNull(), isNull()))
            .thenReturn(mockData);

        reader.setPeriod(PeriodType.ALL_TIME);

        // when
        PowerUserData result = reader.read();

        // then
        assertThat(result).isNotNull();
        assertThat(result.period()).isEqualTo(PeriodType.ALL_TIME);
    }

    @Test
    void Period가_설정되지_않은_경우_null을_반환() throws Exception {
        // when
        PowerUserData result = reader.read();

        // then
        assertThat(result).isNull();
    }

    @Test
    void 빈_데이터_목록인_경우_null을_반환() throws Exception {
        // given
        when(powerUserRepository.findUserActivityData(eq(PeriodType.DAILY), any(Instant.class), any(Instant.class)))
            .thenReturn(List.of());

        reader.setPeriod(PeriodType.DAILY);

        // when
        PowerUserData result = reader.read();

        // then
        assertThat(result).isNull();
    }

    @Test
    void createForPeriod_팩토리_메서드가_정상_동작() throws Exception {
        // given
        List<PowerUserData> mockData = createMockPowerUserData(PeriodType.DAILY);
        when(powerUserRepository.findUserActivityData(eq(PeriodType.DAILY), any(Instant.class), any(Instant.class)))
            .thenReturn(mockData);

        // when
        PowerUserDataReader factoryReader = PowerUserDataReader.createForPeriod(powerUserRepository, PeriodType.DAILY);
        PowerUserData result = factoryReader.read();

        // then
        assertThat(result).isNotNull();
        assertThat(result.period()).isEqualTo(PeriodType.DAILY);
        assertThat(factoryReader.getPowerUserRepository()).isEqualTo(powerUserRepository);
    }

    @Test
    void setPeriod_호출_시_초기화_상태가_리셋() throws Exception {
        // given
        List<PowerUserData> dailyData = createMockPowerUserData(PeriodType.DAILY);
        List<PowerUserData> weeklyData = createMockPowerUserDataForWeekly();

        when(powerUserRepository.findUserActivityData(eq(PeriodType.DAILY), any(Instant.class), any(Instant.class)))
            .thenReturn(dailyData);
        when(powerUserRepository.findUserActivityData(eq(PeriodType.WEEKLY), any(Instant.class), any(Instant.class)))
            .thenReturn(weeklyData);

        reader.setPeriod(PeriodType.DAILY);
        PowerUserData dailyResult = reader.read();

        // when - 새로운 period 설정
        reader.setPeriod(PeriodType.WEEKLY);
        PowerUserData weeklyResult = reader.read();

        // then
        assertThat(dailyResult.period()).isEqualTo(PeriodType.DAILY);
        assertThat(weeklyResult.period()).isEqualTo(PeriodType.WEEKLY);
    }

    private List<PowerUserData> createMockPowerUserData(PeriodType period) {
        User user1 = User.builder()
            .email("test1@example.com")
            .nickname("테스트유저1")
            .password("password")
            .build();

        User user2 = User.builder()
            .email("test2@example.com")
            .nickname("테스트유저2")
            .password("password")
            .build();

        PowerUserData data1 = new PowerUserData(user1, period, 80.0, 10L, 5L);
        PowerUserData data2 = new PowerUserData(user2, period, 70.0, 8L, 4L);

        return List.of(data1, data2);
    }

    private List<PowerUserData> createMockPowerUserDataForWeekly() {
        User user1 = User.builder()
            .email("test1@example.com")
            .nickname("테스트유저1")
            .password("password")
            .build();

        PowerUserData data1 = new PowerUserData(user1, PeriodType.WEEKLY, 100.0, 15L, 8L);
        return List.of(data1);
    }
}