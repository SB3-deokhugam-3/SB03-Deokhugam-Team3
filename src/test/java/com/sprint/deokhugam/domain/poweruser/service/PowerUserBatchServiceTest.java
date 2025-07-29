package com.sprint.deokhugam.domain.poweruser.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sprint.deokhugam.domain.popularreview.service.PopularReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
@DisplayName("PowerUserBatchService 테스트")
class PowerUserBatchServiceTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job powerUserJob;

    @Mock
    private JobExecution jobExecution;

    @Mock
    private PopularReviewService popularReviewService;

    @InjectMocks
    private PowerUserBatchService powerUserBatchService;

    @Test
    void 일간_파워유저_배치_정상_실행() throws Exception {
        // given
        doNothing().when(popularReviewService).validateJobNotDuplicated(any());
        doReturn(jobExecution).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        doReturn(BatchStatus.COMPLETED).when(jobExecution).getStatus();

        // when
        powerUserBatchService.calculateDailyPowerUsers();

        // then
        verify(popularReviewService).validateJobNotDuplicated(any());
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        verify(jobExecution).getStatus();
    }


    @Test
    void 주간_파워유저_배치_정상_실행() throws Exception {
        // given
        doReturn(jobExecution).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        doReturn(BatchStatus.COMPLETED).when(jobExecution).getStatus();

        // when
        powerUserBatchService.calculateWeeklyPowerUsers();

        // then
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        verify(jobExecution).getStatus();
    }

    @Test
    void 월간_파워유저_배치_정상_실행() throws Exception {
        // given
        doReturn(jobExecution).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        doReturn(BatchStatus.COMPLETED).when(jobExecution).getStatus();

        // when
        powerUserBatchService.calculateMonthlyPowerUsers();

        // then
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        verify(jobExecution).getStatus();
    }

    @Test
    void 역대_파워유저_배치_정상_실행() throws Exception {
        // given
        doReturn(jobExecution).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        doReturn(BatchStatus.COMPLETED).when(jobExecution).getStatus();

        // when
        powerUserBatchService.calculateAllTimePowerUsers();

        // then
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        verify(jobExecution).getStatus();
    }

    @Test
    void 일간_파워유저_배치_실행_실패_상태() throws Exception {
        // given
        doNothing().when(popularReviewService).validateJobNotDuplicated(any());
        doReturn(jobExecution).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        doReturn(BatchStatus.FAILED).when(jobExecution).getStatus();

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            powerUserBatchService.calculateDailyPowerUsers();
        });

        // then
        verify(popularReviewService).validateJobNotDuplicated(any());
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        verify(jobExecution, times(2)).getStatus();
        assert (exception.getMessage().contains("DAILY 파워 유저 배치 실행 실패"));
    }

    @Test
    void 주간_파워유저_배치_실행_실패_상태() throws Exception {
        // given
        doReturn(jobExecution).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        doReturn(BatchStatus.FAILED).when(jobExecution).getStatus();

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            powerUserBatchService.calculateWeeklyPowerUsers();
        });

        // then
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        verify(jobExecution, times(2)).getStatus();
        assert (exception.getMessage().contains("WEEKLY 파워 유저 배치 실행 실패"));
    }

    @Test
    void 월간_파워유저_배치_실행_실패_상태() throws Exception {
        // given
        doReturn(jobExecution).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        doReturn(BatchStatus.FAILED).when(jobExecution).getStatus();

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            powerUserBatchService.calculateMonthlyPowerUsers();
        });

        // then
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        verify(jobExecution, times(2)).getStatus();
        assert (exception.getMessage().contains("MONTHLY 파워 유저 배치 실행 실패"));
    }

    @Test
    void 역대_파워유저_배치_실행_실패_상태() throws Exception {
        // given
        doReturn(jobExecution).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        doReturn(BatchStatus.FAILED).when(jobExecution).getStatus();

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            powerUserBatchService.calculateAllTimePowerUsers();
        });

        // then
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        verify(jobExecution, times(2)).getStatus();
        assert (exception.getMessage().contains("ALL_TIME 파워 유저 배치 실행 실패"));
    }

    @Test
    void 일간_파워유저_배치_실행_예외_발생() throws Exception {
        // given
        doNothing().when(popularReviewService).validateJobNotDuplicated(any());
        RuntimeException testException = new RuntimeException("배치 실행 중 예외 발생");
        doThrow(testException).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            powerUserBatchService.calculateDailyPowerUsers();
        });

        // then
        verify(popularReviewService).validateJobNotDuplicated(any());
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        assert (exception.getMessage().contains("DAILY 파워 유저 배치 실행 실패"));
        assert (exception.getCause() == testException);
    }

    @Test
    void 주간_파워유저_배치_실행_예외_발생() throws Exception {
        // given
        RuntimeException testException = new RuntimeException("배치 실행 중 예외 발생");
        doThrow(testException).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            powerUserBatchService.calculateWeeklyPowerUsers();
        });

        // then
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        assert (exception.getMessage().contains("WEEKLY 파워 유저 배치 실행 실패"));
        assert (exception.getCause() == testException);
    }

    @Test
    void 월간_파워유저_배치_실행_예외_발생() throws Exception {
        // given
        RuntimeException testException = new RuntimeException("배치 실행 중 예외 발생");
        doThrow(testException).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            powerUserBatchService.calculateMonthlyPowerUsers();
        });

        // then
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        assert (exception.getMessage().contains("MONTHLY 파워 유저 배치 실행 실패"));
        assert (exception.getCause() == testException);
    }

    @Test
    void 역대_파워유저_배치_실행_예외_발생() throws Exception {
        // given
        RuntimeException testException = new RuntimeException("배치 실행 중 예외 발생");
        doThrow(testException).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            powerUserBatchService.calculateAllTimePowerUsers();
        });

        // then
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        assert (exception.getMessage().contains("ALL_TIME 파워 유저 배치 실행 실패"));
        assert (exception.getCause() == testException);
    }

    @Test
    void 배치_실행_상태_진행중() throws Exception {
        // given
        doNothing().when(popularReviewService).validateJobNotDuplicated(any());
        doReturn(jobExecution).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        doReturn(BatchStatus.STARTED).when(jobExecution).getStatus();

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            powerUserBatchService.calculateDailyPowerUsers();
        });

        // then
        verify(popularReviewService).validateJobNotDuplicated(any());
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        verify(jobExecution, times(2)).getStatus(); // 2번 호출 허용
        assert (exception.getMessage().contains("DAILY 파워 유저 배치 실행 실패"));
    }

    @Test
    void 배치_실행_상태_포기됨() throws Exception {
        // given
        doReturn(jobExecution).when(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        doReturn(BatchStatus.ABANDONED).when(jobExecution).getStatus();

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            powerUserBatchService.calculateWeeklyPowerUsers();
        });

        // then
        verify(jobLauncher).run(eq(powerUserJob), any(JobParameters.class));
        verify(jobExecution, times(2)).getStatus(); // 2번 호출 허용
        assert (exception.getMessage().contains("WEEKLY 파워 유저 배치 실행 실패"));
    }
}