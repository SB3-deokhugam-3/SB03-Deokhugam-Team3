package com.sprint.deokhugam.domain.popularreview.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job popularReviewJob;

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
//    @Scheduled(cron = "0/10 * * * * *", zone = "Asia/Seoul") // 10초마다 실행(테스트용)
    public void runPopularReviewJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
        jobLauncher.run(popularReviewJob, params);
    }
}
