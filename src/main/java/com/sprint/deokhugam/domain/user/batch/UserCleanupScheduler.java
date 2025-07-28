package com.sprint.deokhugam.domain.user.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job userCleanupJob;

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
//    @Scheduled(cron = "0/10 * * * * *", zone = "Asia/Seoul") // 10초마다 실행(테스트용)
    public void runUserCleanupBatch() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis()) // 매번 다른 파라미터로 실행
                .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(userCleanupJob, jobParameters);
            log.info("배치 작업 완료: {}", jobExecution.getStatus());

        } catch (Exception e) {
            log.error("배치 작업 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }

}
