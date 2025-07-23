package com.sprint.deokhugam.domain.poweruser.scheduler;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

import com.sprint.deokhugam.domain.poweruser.service.PowerUserBatchService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
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
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);

        // when
        powerUserScheduler.schedulePowerUserCalculation();

        // then
        verify(jobLauncher, times(1)).run(eq(powerUserJob), any(JobParameters.class));
        verify(jobExecution, times(2)).getStatus();
    }

    @Test
    void schedulePowerUserCalculation_실패_시_예외_처리() throws Exception {
        // given
        when(jobLauncher.run(eq(powerUserJob), any(JobParameters.class)))
            .thenThrow(new RuntimeException("배치 작업 실패"));

        // when
        powerUserScheduler.schedulePowerUserCalculation();

        // then
        verify(jobLauncher, times(1)).run(eq(powerUserJob), any(JobParameters.class));
    }

    @Test
    void schedulePowerUserCalculation_성공_로그_확인() throws Exception {
        // given
        JobExecution successExecution = new JobExecution(1L);
        successExecution.setExitStatus(ExitStatus.COMPLETED);
        when(jobLauncher.run(eq(powerUserJob), any(JobParameters.class)))
            .thenReturn(successExecution);

        // when
        powerUserScheduler.schedulePowerUserCalculation();

        // then
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
    }

    @Test
    void schedulePowerUserCalculation_JobParameters_생성_확인() throws Exception {
        // given
        ArgumentCaptor<JobParameters> parametersCaptor =
            ArgumentCaptor.forClass(JobParameters.class);

        // when
        powerUserScheduler.schedulePowerUserCalculation();

        // then
        verify(jobLauncher).run(eq(powerUserJob), parametersCaptor.capture());
        JobParameters capturedParams = parametersCaptor.getValue();
        assertThat(capturedParams.getParameters()).containsKey("timestamp");
    }
}

