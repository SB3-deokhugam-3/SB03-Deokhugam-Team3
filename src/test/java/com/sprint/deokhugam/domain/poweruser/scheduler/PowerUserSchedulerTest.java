package com.sprint.deokhugam.domain.poweruser.scheduler;

import static org.mockito.Mockito.*;

import com.sprint.deokhugam.domain.poweruser.service.PowerUserBatchService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@DisplayName("PowerUserScheduler 테스트")
@ActiveProfiles("test")
class PowerUserSchedulerTest {

    @Mock
    private PowerUserBatchService powerUserBatchService;

    private MeterRegistry meterRegistry;

    @InjectMocks
    private PowerUserScheduler powerUserScheduler;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        powerUserScheduler = new PowerUserScheduler(powerUserBatchService, meterRegistry);
    }

    @Test
    void schedulePowerUserCalculation_정상_실행() {
        // given
        doNothing().when(powerUserBatchService).calculateDailyPowerUsers();
        doNothing().when(powerUserBatchService).calculateAllTimePowerUsers();

        // when
        powerUserScheduler.schedulePowerUserCalculation();

        // then
        verify(powerUserBatchService).calculateDailyPowerUsers();
        verify(powerUserBatchService).calculateAllTimePowerUsers();
        // 주간, 월간은 특정 요일/날짜에만 실행되므로 이 테스트에서는 호출되지 않음
    }

    @Test
    void schedulePowerUserCalculation_일부_실패_시_다른_작업_계속_실행() {
        // given
        doThrow(new RuntimeException("일간 계산 실패")).when(powerUserBatchService).calculateDailyPowerUsers();
        doNothing().when(powerUserBatchService).calculateAllTimePowerUsers();

        // when
        powerUserScheduler.schedulePowerUserCalculation();

        // then
        verify(powerUserBatchService).calculateDailyPowerUsers();
        verify(powerUserBatchService).calculateAllTimePowerUsers(); // 실패에도 불구하고 실행됨
    }

    @Test
    void schedulePowerUserCalculation_중복_실행_방지() throws InterruptedException {
        // given
        doAnswer(invocation -> {
            Thread.sleep(100); // 작업 시뮬레이션
            return null;
        }).when(powerUserBatchService).calculateDailyPowerUsers();

        // when - 동시에 두 번 실행
        Thread thread1 = new Thread(() -> powerUserScheduler.schedulePowerUserCalculation());
        Thread thread2 = new Thread(() -> powerUserScheduler.schedulePowerUserCalculation());

        thread1.start();
        Thread.sleep(10); // 첫 번째 스레드가 먼저 시작되도록
        thread2.start();

        thread1.join();
        thread2.join();

        // then - 실제로는 한 번만 실행되어야 함
        verify(powerUserBatchService, atMost(1)).calculateDailyPowerUsers();
    }
}

