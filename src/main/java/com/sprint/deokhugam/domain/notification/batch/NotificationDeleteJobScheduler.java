package com.sprint.deokhugam.domain.notification.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDeleteJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job notificationDeleteJob;

    @Scheduled(cron = "00 00 00 * * *")
    public void runNotificationDeleteJob() {
        try {
            jobLauncher.run(notificationDeleteJob,
                new org.springframework.batch.core.JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters()
            );
        } catch (Exception e) {
            log.error("알림 삭제 배치 작업 실행 중 오류 발생", e);
        }

    }
}
