package com.sprint.deokhugam.global.batch;

import com.sprint.deokhugam.domain.review.entity.Review;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory emf;
    private final BatchListener listener;

    private final String REVIEW_BATCH_NAME = "POPULAR_REVIEW_RANKING";
    //TODO:  나중에 enum으로 빼기
    private final String REVIEW_BATCH_PERIOD_ALL_TIME = "ALL_TIME";
    private final String REVIEW_BATCH_PERIOD_MONTHLY = "MONTHLY";
    private final String REVIEW_BATCH_PERIOD_WEEKLY = "WEEKLY";
    private final String REVIEW_BATCH_PERIOD_DAILY = "DAILY";

    private final Integer REVIEW_BATCH_PERIOD_ALL_TIME_DATE = null;
    private final Integer REVIEW_BATCH_PERIOD_MONTHLY_DATE = 30;
    private final Integer REVIEW_BATCH_PERIOD_WEEKLY_DATE = 7;
    private final Integer REVIEW_BATCH_PERIOD_DAILY_DATE = 1;

    @Bean
    public Job reviewJob() {
        return new JobBuilder(REVIEW_BATCH_NAME, jobRepository)
            //Automatically increments job execution number (run.id), Used for execution history management
            .incrementer(new RunIdIncrementer())
            .start(reviewStep())
            .build();
    }

    @Bean
    public Step reviewStep() {
        return new StepBuilder(REVIEW_BATCH_NAME + "-STEP", jobRepository)
            .<Review, String>chunk(100, transactionManager)
            .listener(listener)
            .reader(reviewReader())
            .processor(reviewProcessor())
            .writer(reviewWriter())
            .build();
    }

    @Bean
    public JpaPagingItemReader<Review> reviewReader() {
        JpaPagingItemReader<Review> reader = new JpaPagingItemReader<>();
        reader.setQueryString("SELECT r FROM Review r");
        reader.setEntityManagerFactory(emf);
        reader.setPageSize(10);
        reader.setName("reviewBuilder");
        return reader;
    }

    @Bean
    public ItemProcessor<Review, String> reviewProcessor() {
        return review -> {
            try {
                log.info("✅ Processed review: id={}", review.getId());
                return review.getId().toString();
            } catch (Exception e) {
                log.error("❌ Failed to process review: id={}", review.getId(), e);
                throw e; // throw 해서 Step 실패 유도
            }
        };
    }

    @Bean
    public ItemWriter<String> reviewWriter() {
        return reviewEmails -> reviewEmails.forEach(System.out::println);
    }
}
