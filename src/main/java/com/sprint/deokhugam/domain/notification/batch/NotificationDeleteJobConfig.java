package com.sprint.deokhugam.domain.notification.batch;

import com.sprint.deokhugam.domain.notification.entity.Notification;
import com.sprint.deokhugam.domain.notification.repository.NotificationRepository;
import jakarta.persistence.EntityManagerFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class NotificationDeleteJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final NotificationRepository notificationRepository;

    private static final String JOB_NAME = "notificationDeleteJob";
    private static final String STEP_NAME = "notificationDeleteStep";

    @Bean
    public Job notificationDeleteJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(notificationDeleteStep())
            .build();
    }

    @Bean
    public Step notificationDeleteStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
            .<Notification, Notification>chunk(100, transactionManager)
            .reader(notificationItemReader())
            .writer(notificationItemWriter())
            .build();
    }

    @Bean
    public JpaPagingItemReader<Notification> notificationItemReader() {
        return new JpaPagingItemReaderBuilder<Notification>()
            .name("notificationItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("""
                    SELECT n FROM Notification n
                    WHERE n.isConfirmed = true
                    AND n.updatedAt < :oneWeekAgo
                """)
            .parameterValues(Map.of("oneWeekAgo", Instant.now().minus(7, ChronoUnit.DAYS)))
            .pageSize(100)
            .build();
    }

    @Bean
    public ItemWriter<Notification> notificationItemWriter() {
        return notifications -> notificationRepository.deleteAllInBatch(
            notifications.getItems().stream().map(n -> (Notification) n).toList()
        );
    }
}

