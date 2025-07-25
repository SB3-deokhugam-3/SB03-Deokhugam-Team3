package com.sprint.deokhugam.domain.poweruser.scheduler;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class PowerUserScheduler {

    private final JobLauncher jobLauncher;
    private final Job powerUserJob;
    private final JobExplorer jobExplorer;
    private final MeterRegistry meterRegistry;

    // 중복 실행 방지를 위한 플래그
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * 매일 00:05에 파워 유저 데이터 계산
     */
    @Scheduled(cron = "0/30 * * * * *", zone = "Asia/Seoul")
    public void schedulePowerUserCalculation() {
        String executionDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        // 중복 실행 체크 (AtomicBoolean)
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("이전 파워 유저 배치 작업이 아직 실행 중입니다. 스킵합니다.");
            meterRegistry.counter("poweruser.batch.skipped.atomic").increment();
            return;
        }

        try {
            // Spring Batch Job Instance 중복 실행 체크
            if (isDuplicateJobExecution(executionDate)) {
                log.warn("오늘 날짜({})에 대한 파워 유저 배치 작업이 이미 완료되었습니다. 스킵합니다.", executionDate);
                meterRegistry.counter("poweruser.batch.skipped.duplicate").increment();
                return;
            }

            Timer.Sample sample = Timer.start(meterRegistry);

            try {
                log.info("파워 유저 배치 작업 시작 - 실행 날짜: {}", executionDate);

                // Spring Batch Job 실행 (날짜별 고유한 Job Instance 생성)
                JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executionDate", executionDate)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

                JobExecution jobExecution = jobLauncher.run(powerUserJob, jobParameters);

                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    meterRegistry.counter("poweruser.batch.success").increment();
                    log.info("파워 유저 배치 작업 완료 - Status: {}, 실행 날짜: {}", jobExecution.getStatus(), executionDate);
                } else {
                    meterRegistry.counter("poweruser.batch.failure").increment();
                    log.error("파워 유저 배치 작업 실패 - Status: {}, Exit Code: {}, 실행 날짜: {}",
                        jobExecution.getStatus(), jobExecution.getExitStatus(), executionDate);
                }

            } catch (JobExecutionAlreadyRunningException e) {
                log.warn("파워 유저 배치 작업이 이미 실행 중입니다 - 실행 날짜: {}", executionDate);
                meterRegistry.counter("poweruser.batch.skipped.running").increment();
            } catch (JobInstanceAlreadyCompleteException e) {
                log.warn("파워 유저 배치 작업이 이미 완료되었습니다 - 실행 날짜: {}", executionDate);
                meterRegistry.counter("poweruser.batch.skipped.completed").increment();
            } catch (JobRestartException e) {
                log.error("파워 유저 배치 작업 재시작 오류 - 실행 날짜: {}", executionDate, e);
                meterRegistry.counter("poweruser.batch.restart_error").increment();
            } catch (Exception e) {
                meterRegistry.counter("poweruser.batch.total_failure").increment();
                log.error("파워 유저 배치 작업 전체 실패 - 실행 날짜: {}", executionDate, e);
            } finally {
                sample.stop(Timer.builder("poweruser.batch.duration")
                    .description("PowerUser batch processing time")
                    .register(meterRegistry));
            }

        } finally {
            isRunning.set(false);
        }
    }

    /**
     * 오늘 날짜에 대한 Job이 이미 성공적으로 실행되었는지 확인
     */
    private boolean isDuplicateJobExecution(String executionDate) {
        try {
            JobInstance jobInstance = jobExplorer.getJobInstance(powerUserJob.getName(),
                new JobParametersBuilder()
                    .addString("executionDate", executionDate)
                    .toJobParameters());

            if (jobInstance != null) {
                // 해당 Job Instance의 최신 실행 상태 확인
                JobExecution lastExecution = jobExplorer.getLastJobExecution(jobInstance);
                if (lastExecution != null && lastExecution.getStatus() == BatchStatus.COMPLETED) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("Job 중복 실행 체크 중 오류 발생: {}", e.getMessage());
            return false; // 확인할 수 없으면 실행 허용
        }
    }
}
