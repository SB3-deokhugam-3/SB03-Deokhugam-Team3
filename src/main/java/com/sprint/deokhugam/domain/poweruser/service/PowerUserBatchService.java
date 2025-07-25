package com.sprint.deokhugam.domain.poweruser.service;

import com.sprint.deokhugam.domain.popularreview.service.PopularReviewService;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.global.enums.PeriodType;
import com.sprint.deokhugam.global.exception.BatchAlreadyRunException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PowerUserBatchService {

    private final JobLauncher jobLauncher;
    private final PopularReviewService popularReviewService;
    private final PowerUserRepository powerUserRepository;

    // 각 기간별 개별 Job 필드 주입
    @Autowired
    @Qualifier("dailyPowerUserJob")
    private Job dailyPowerUserJob;

    @Autowired
    @Qualifier("weeklyPowerUserJob")
    private Job weeklyPowerUserJob;

    @Autowired
    @Qualifier("monthlyPowerUserJob")
    private Job monthlyPowerUserJob;

    @Autowired
    @Qualifier("allTimePowerUserJob")
    private Job allTimePowerUserJob;

    // 생성자는 다른 의존성만 주입
    public PowerUserBatchService(JobLauncher jobLauncher,
        PopularReviewService popularReviewService,
        PowerUserRepository powerUserRepository) {
        this.jobLauncher = jobLauncher;
        this.popularReviewService = popularReviewService;
        this.powerUserRepository = powerUserRepository;
    }

    /**
     * 일간 파워 유저 배치 실행
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void calculateDailyPowerUsers() {
        log.info("일간 파워 유저 배치 실행 시작");

        if (shouldSkipExecution(PeriodType.DAILY)) {
            log.info("오늘 이미 DAILY 배치가 실행되었습니다. 스킵합니다.");
            return;
        }

        // 먼저 기존 데이터 삭제
        int deletedCount = powerUserRepository.deleteByPeriod(PeriodType.DAILY);
        log.info("기존 DAILY 파워유저 데이터 {} 건 삭제", deletedCount);

        executePowerUserBatch(dailyPowerUserJob, PeriodType.DAILY);
    }

    /**
     * 주간 파워 유저 배치 실행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateWeeklyPowerUsers() {
        log.info("주간 파워 유저 배치 실행 시작");

        int deletedCount = powerUserRepository.deleteByPeriod(PeriodType.WEEKLY);
        log.info("기존 WEEKLY 파워유저 데이터 {} 건 삭제", deletedCount);

        executePowerUserBatch(weeklyPowerUserJob, PeriodType.WEEKLY);
    }

    /**
     * 월간 파워 유저 배치 실행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateMonthlyPowerUsers() {
        log.info("월간 파워 유저 배치 실행 시작");

        int deletedCount = powerUserRepository.deleteByPeriod(PeriodType.MONTHLY);
        log.info("기존 MONTHLY 파워유저 데이터 {} 건 삭제", deletedCount);

        executePowerUserBatch(monthlyPowerUserJob, PeriodType.MONTHLY);
    }

    /**
     * 역대 파워 유저 배치 실행
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateAllTimePowerUsers() {
        log.info("역대 파워 유저 배치 실행 시작");

        int deletedCount = powerUserRepository.deleteByPeriod(PeriodType.ALL_TIME);
        log.info("기존 ALL_TIME 파워유저 데이터 {} 건 삭제", deletedCount);

        executePowerUserBatch(allTimePowerUserJob, PeriodType.ALL_TIME);
    }

    /**
     * 기간별 파워 유저 배치 실행
     */
    private void executePowerUserBatch(Job job, PeriodType periodType) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("period", periodType.name())
                .addString("executionDate", LocalDate.now().toString())
                .toJobParameters();

            log.info("{} 배치 실행 시작 - Job: {}", periodType, job.getName());
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);

            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                log.info("{} 파워 유저 배치 실행 완료 - Status: {}", periodType, jobExecution.getStatus());

                // 실행 후 결과 확인
                long resultCount = powerUserRepository.countByPeriod(periodType);
                log.info("{} 배치 실행 결과: {} 건의 파워유저 데이터 생성됨", periodType, resultCount);

            } else {
                log.error("{} 파워 유저 배치 실행 실패 - Status: {}, ExitCode: {}",
                    periodType, jobExecution.getStatus(), jobExecution.getExitStatus());
                throw new RuntimeException(periodType + " 파워 유저 배치 실행 실패");
            }

        } catch (Exception e) {
            log.error("{} 파워 유저 배치 실행 중 오류 발생", periodType, e);
            throw new RuntimeException(periodType + " 파워 유저 배치 실행 실패", e);
        }
    }

    private boolean isAlreadyRunToday(PeriodType period) {
        try {
            List<PowerUser> existingData = powerUserRepository.findTopPowerUsersNByPeriod(period, 1);
            if (!existingData.isEmpty()) {
                LocalDate today = LocalDate.now();
                LocalDate dataDate = existingData.get(0).getCreatedAt()
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toLocalDate();
                return dataDate.equals(today);
            }
        } catch (Exception e) {
            log.debug("중복 실행 체크 중 오류 (무시됨): {}", e.getMessage());
        }
        return false;
    }

    private boolean shouldSkipExecution(PeriodType period) {
        // 개발 환경에서는 항상 실행
        String activeProfile = System.getProperty("spring.profiles.active", "local");
        if ("local".equals(activeProfile) || "dev".equals(activeProfile)) {
            log.info("개발 환경이므로 중복 체크를 건너뜁니다.");
            return false;
        }

        return isAlreadyRunToday(period);
    }
}