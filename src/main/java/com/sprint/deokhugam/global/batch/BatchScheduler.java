package com.sprint.deokhugam.global.batch;

import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job popularReviewJob;
    private final Job popularBookRankingJob;
    private final Job powerUserJob;


    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void runPopularReviewJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
        jobLauncher.run(popularReviewJob, params);
    }

    @Scheduled(cron = "0 6 0 * * *", zone = "Asia/Seoul")
    public void runPopularBookRankingJob() {
        Instant today = Instant.now();

        for (PeriodType period : PeriodType.values()) {
            try {
                JobParameters jobParameters = new JobParametersBuilder()
                    .addString("period", period.name())
                    .addString("today", today.toString())
                    .addLong("timestamp", System.currentTimeMillis()) // 유니크 파라미터
                    .toJobParameters();

                log.info("[PopularBookRankingScheduler] 인기 도서 랭킹 배치 실행 - period: {}, today: {}",
                    period, today);

                jobLauncher.run(popularBookRankingJob, jobParameters);
            } catch (Exception e) {
                log.warn("[PopularBookRankingScheduler] 인기 도서 랭킹 배치 실패 : {}", e.getMessage());
            }
        }
    }

    // PowerUser Job 스케줄링 추가
    @Scheduled(cron = "0 7 0 * * *", zone = "Asia/Seoul") // 1분마다 실행
    public void runPowerUserJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis()) // 유니크 파라미터
                .addString("today", Instant.now().toString())
                .toJobParameters();

            log.info("[PowerUserScheduler] 파워 유저 배치 실행 시작");
            jobLauncher.run(powerUserJob, jobParameters);
            log.info("[PowerUserScheduler] 파워 유저 배치 실행 완료");
        } catch (Exception e) {
            log.error("[PowerUserScheduler] 파워 유저 배치 실패: {}", e.getMessage(), e);
        }
    }
}
