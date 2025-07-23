package com.sprint.deokhugam.domain.notification.batch;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationDeleteJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job notificationDeleteJob;

    @Scheduled(cron = "00 00 00 * * *")
    public void runNotificationDeleteJob() throws Exception {
        jobLauncher.run(notificationDeleteJob,
            new org.springframework.batch.core.JobParametersBuilder()
                .addDate("runTime", new Date())
                .toJobParameters()
        );
    }
}
