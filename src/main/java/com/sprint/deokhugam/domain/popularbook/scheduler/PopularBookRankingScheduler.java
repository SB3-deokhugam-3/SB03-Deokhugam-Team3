package com.sprint.deokhugam.domain.popularbook.scheduler;

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
@Component
@RequiredArgsConstructor
public class PopularBookRankingScheduler {

    private final JobLauncher jobLauncher;
    private final Job popularBookRankingJob;

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void runJob() {
        Instant today = Instant.now();

        for (PeriodType period : PeriodType.values()) {
            try {
                JobParameters jobParameters = new JobParametersBuilder()
                    .addString("period", period.name())
                    .addString("today", today.toString())
                    .addLong("timestamp", System.currentTimeMillis()) // 유니크 파라미터
                    .toJobParameters();

                log.info("[PopularBookRankingScheduler] 인기 도서 랭킹 배치 실행 - period: {}, today: {}", period, today);

                jobLauncher.run(popularBookRankingJob, jobParameters);
            } catch (Exception e) {
                log.warn("[PopularBookRankingScheduler] 인기 도서 랭킹 배치 실패 : {}", e.getMessage());
            }
        }
    }
}
