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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@DisplayName("PowerUserScheduler 테스트")
@ActiveProfiles("test")
class PowerUserSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job powerUserJob;

    @Mock
    private JobExecution jobExecution;

    private MeterRegistry meterRegistry;

    @InjectMocks
    private PowerUserScheduler powerUserScheduler;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        powerUserScheduler = new PowerUserScheduler(jobLauncher, powerUserJob, meterRegistry);
    }

    @Test
    void schedulePowerUserCalculation_정상_실행() throws Exception {
        // given
        when(jobLauncher.run(eq(powerUserJob), any(JobParameters.class)))
            .thenReturn(jobExecution);
        when(jobExecution.getJobId()).thenReturn(1L);

        // when
        powerUserScheduler.schedulePowerUserCalculation();

        // then
        // 일간 + 역대 = 최소 2번은 실행되어야 함
        verify(jobLauncher, atLeast(2)).run(eq(powerUserJob), any(JobParameters.class));
    }

    @Test
    void schedulePowerUserCalculation_일부_실패_시_다른_작업_계속_실행() throws Exception {
        // given
        when(jobLauncher.run(eq(powerUserJob), any(JobParameters.class)))
            .thenThrow(new RuntimeException("일간 계산 실패"))  // 첫 번째 호출에서 실패
            .thenReturn(jobExecution);  // 두 번째 호출에서 성공
        when(jobExecution.getJobId()).thenReturn(1L);

        // when
        powerUserScheduler.schedulePowerUserCalculation();

        // then
        verify(jobLauncher, atLeast(2)).run(eq(powerUserJob), any(JobParameters.class));
    }
}

