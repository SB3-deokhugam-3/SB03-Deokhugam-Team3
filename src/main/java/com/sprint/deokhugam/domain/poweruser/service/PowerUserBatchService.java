package com.sprint.deokhugam.domain.poweruser.service;

import com.sprint.deokhugam.domain.popularreview.service.PopularReviewService;
import com.sprint.deokhugam.global.enums.PeriodType;
import com.sprint.deokhugam.global.exception.BatchAlreadyRunException;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PowerUserBatchService {

    private final JobLauncher jobLauncher;
    private final Job powerUserJob;
    private final PopularReviewService popularReviewService;

    /**
     * 일간 파워 유저 배치 실행
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void calculateDailyPowerUsers() {
        log.info("일간 파워 유저 배치 실행 시작");
        validatePopularReviewDataExists(PeriodType.DAILY);
        executePowerUserBatch(PeriodType.DAILY);
    }

    /**
     * 주간 파워 유저 배치 실행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateWeeklyPowerUsers() {
        log.info("주간 파워 유저 배치 실행 시작");
        executePowerUserBatch(PeriodType.WEEKLY);
    }

    /**
     * 월간 파워 유저 배치 실행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateMonthlyPowerUsers() {
        log.info("월간 파워 유저 배치 실행 시작");
        executePowerUserBatch(PeriodType.MONTHLY);
    }

    /**
     * 역대 파워 유저 배치 실행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateAllTimePowerUsers() {
        log.info("역대 파워 유저 배치 실행 시작");
        executePowerUserBatch(PeriodType.ALL_TIME);
    }

    /**
     * 기간별 파워 유저 배치 실행
     */
    private void executePowerUserBatch(PeriodType periodType) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("period", periodType.name())
                .addString("executionDate", LocalDate.now().toString())
                .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(powerUserJob, jobParameters);

            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                log.info("{} 파워 유저 배치 실행 완료", periodType);
            } else {
                log.error("{} 파워 유저 배치 실행 실패 - Status: {}",
                    periodType, jobExecution.getStatus());
                throw new RuntimeException(periodType + " 파워 유저 배치 실행 실패");
            }

        } catch (Exception e) {
            log.error("{} 파워 유저 배치 실행 중 오류 발생", periodType, e);
            throw new RuntimeException(periodType + " 파워 유저 배치 실행 실패", e);
        }
    }

    private void validatePopularReviewDataExists(PeriodType period) {
        try {
            popularReviewService.validateJobNotDuplicated(Instant.now());
            log.info("{} 기간 PopularReview 데이터 존재 확인 완료", period);
        } catch (BatchAlreadyRunException e) {
            log.debug("{} 기간 PopularReview 데이터가 이미 존재함", period);
        }
    }
}