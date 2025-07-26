package com.sprint.deokhugam.global.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sprint.deokhugam.global.enums.PeriodType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

@ExtendWith(MockitoExtension.class)
class BatchSchedulerTest {

    @InjectMocks
    private BatchScheduler scheduler;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job popularReviewJob;

    @Mock
    private Job popularBookRankingJob;

    @Mock
    private Job powerUserJob;

    @Test
    void 인기_리뷰_배치_정상_실행() throws Exception {
        // given
        JobExecution mockExecution = new JobExecution(1L);
        given(jobLauncher.run(any(Job.class), any(JobParameters.class))).willReturn(mockExecution);

        // when
        scheduler.runPopularReviewJob();

        // then - 실제 코드에서는 popularReviewJob을 실행
        verify(jobLauncher, times(1)).run(eq(popularReviewJob), any(JobParameters.class));

        // JobParameters 검증 - timestamp 파라미터가 포함되어야 함
        ArgumentCaptor<JobParameters> parametersCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(popularReviewJob), parametersCaptor.capture());

        JobParameters capturedParams = parametersCaptor.getValue();
        assertThat(capturedParams.getLong("timestamp")).isNotNull();
    }

    @Test
    void 파워_유저_배치_정상_실행() throws Exception {
        // given
        JobExecution mockExecution = new JobExecution(1L);
        given(jobLauncher.run(any(Job.class), any(JobParameters.class))).willReturn(mockExecution);

        // when
        scheduler.runPowerUserJob();

        // then - 실제 코드에서는 powerUserJob을 실행
        verify(jobLauncher, times(1)).run(eq(powerUserJob), any(JobParameters.class));

        // JobParameters 검증 - today와 timestamp 파라미터가 포함되어야 함
        ArgumentCaptor<JobParameters> parametersCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(powerUserJob), parametersCaptor.capture());

        JobParameters capturedParams = parametersCaptor.getValue();
        assertThat(capturedParams.getString("today")).isNotNull();
        assertThat(capturedParams.getLong("timestamp")).isNotNull();
    }

    @Test
    void 각_기간별_인기_도서_배치_파라미터_검증() throws Exception {
        // given
        JobExecution mockExecution = new JobExecution(1L);
        given(jobLauncher.run(any(Job.class), any(JobParameters.class))).willReturn(mockExecution);

        // when
        scheduler.runPopularBookRankingJob();

        // then - 각 기간별로 popularBookRankingJob을 실행
        verify(jobLauncher, times(PeriodType.values().length))
            .run(eq(popularBookRankingJob), any(JobParameters.class));

        ArgumentCaptor<JobParameters> parametersCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher, times(PeriodType.values().length))
            .run(eq(popularBookRankingJob), parametersCaptor.capture());

        List<JobParameters> capturedParameters = parametersCaptor.getAllValues();

        // 각 기간별로 올바른 파라미터가 전달되었는지 확인
        Set<String> expectedPeriods = Arrays.stream(PeriodType.values())
            .map(Enum::name)
            .collect(Collectors.toSet());
        Set<String> actualPeriods = capturedParameters.stream()
            .map(params -> params.getString("period"))
            .collect(Collectors.toSet());

        assertEquals(expectedPeriods, actualPeriods);

        // 모든 파라미터에 today와 timestamp가 포함되어야 함
        for (JobParameters params : capturedParameters) {
            assertThat(params.getString("today")).isNotNull();
            assertThat(params.getLong("timestamp")).isNotNull();
        }
    }
}