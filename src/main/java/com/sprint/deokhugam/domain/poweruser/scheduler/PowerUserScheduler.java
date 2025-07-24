package com.sprint.deokhugam.domain.poweruser.scheduler;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
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
    private final MeterRegistry meterRegistry;

    // 중복 실행 방지를 위한 플래그
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * 매일 새벽 2시에 파워 유저 데이터 계산
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void schedulePowerUserCalculation() {
        // 중복 실행 체크
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("이전 파워 유저 배치 작업이 아직 실행 중입니다. 스킵합니다.");
            meterRegistry.counter("poweruser.batch.skipped").increment();
            return;
        }

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            log.info("파워 유저 배치 작업 시작");

            // Spring Batch Job 실행 ( 모든 기간 처리 )
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("executionDate", LocalDate.now().toString())
                .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(powerUserJob, jobParameters);

            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                meterRegistry.counter("poweruser.batch.success").increment();
                log.info("파워 유저 배치 작업 완료 - Status: {}", jobExecution.getStatus());
            } else {
                meterRegistry.counter("poweruser.batch.failure").increment();
                log.error("파워 유저 배치 작업 실패 - Status: {}, Exit Code: {}",
                    jobExecution.getStatus(), jobExecution.getExitStatus());
            }

        } catch (Exception e) {
            meterRegistry.counter("poweruser.batch.total_failure").increment();
            log.error("파워 유저 배치 작업 전체 실패", e);
        } finally {
            isRunning.set(false);
            sample.stop(Timer.builder("poweruser.batch.duration")
                .description("PowerUser batch processing time")
                .register(meterRegistry));
        }
    }

    /**
     * 주간 계산 실행일인지 확인 ( 일요일 )
     */
    private boolean isWeeklyCalculationDay() {
        return LocalDate.now().getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /**
     * 월간 계산 실행일인지 확인 ( 매월 1일 )
     */
    private boolean isMonthlyCalculationDay() {
        return LocalDate.now().getDayOfMonth() == 1;
    }
}
