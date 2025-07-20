package com.sprint.deokhugam.domain.poweruser.scheduler;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.sprint.deokhugam.domain.poweruser.service.PowerUserBatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PowerUserScheduler 테스트")
public class PowerUserSchedulerTest {

    @Mock
    private PowerUserBatchService powerUserBatchService;

    @InjectMocks
    private PowerUserScheduler powerUserScheduler;

    @Test
    void schedulePowerUserCalculation_정상_실행() {
        // given
        // 현재 시점을 조작하기 어렵기에 일반적인 실행 테스트

        // when
        powerUserScheduler.schedulePowerUserCalculation();

        // then
        verify(powerUserBatchService).calculateAllTimePowerUsers();
        verify(powerUserBatchService).calculateDailyPowerUsers();
        // 주간, 월간은 특정 요일/날짜에만 실행되기에 별도의 테스트가 필요
    }

    @Test
    void schedulePowerUserCalculation_예외_발생시_로그_기록() {
        // given
        doThrow(new RuntimeException("배치 실행 오류"))
            .when(powerUserBatchService).calculateDailyPowerUsers();

        // when
        Exception exception = null;
        try {
            powerUserScheduler.schedulePowerUserCalculation();
        } catch (Exception e) {
            exception = e;
        }

        // then
        assertNull(exception);
        verify(powerUserBatchService).calculateDailyPowerUsers();
    }
}
