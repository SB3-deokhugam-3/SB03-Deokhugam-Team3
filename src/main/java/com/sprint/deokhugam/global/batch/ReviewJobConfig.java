package com.sprint.deokhugam.global.batch;

import com.sprint.deokhugam.domain.popularreview.service.PopularReviewService;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.service.ReviewService;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ReviewJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchListener listener;

    private final PopularReviewService popularReviewService;
    private final ReviewService reviewService;

    private final String REVIEW_BATCH_NAME = "POPULAR_REVIEW_RANKING";

    @Bean
    public Job reviewJob() {
        return new JobBuilder(REVIEW_BATCH_NAME, jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(reviewStep(jobRepository, transactionManager))
            .build();
    }

    @Bean
    public Step reviewStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("reviewStep", jobRepository)
            .tasklet(reviewTasklet(), transactionManager)
            .listener(listener)
            .build();
    }

    protected Tasklet reviewTasklet() {
        return (stepContribution, chunkContext) -> {

            try {
                /* 오늘 이미 실행한 배치인지 검증 */
                popularReviewService.validateTodayJobNotDuplicated();

                List<Review> totalReviews = reviewService.findPopularReviewCandidates();

                popularReviewService.savePopularReviewsByPeriod(totalReviews,
                    PeriodType.ALL_TIME, stepContribution);
                popularReviewService.savePopularReviewsByPeriod(totalReviews,
                    PeriodType.MONTHLY, stepContribution);
                popularReviewService.savePopularReviewsByPeriod(totalReviews,
                    PeriodType.WEEKLY, stepContribution);
                popularReviewService.savePopularReviewsByPeriod(totalReviews,
                    PeriodType.DAILY, stepContribution);

            } catch (Exception e) {
                log.error(e.getMessage());
                stepContribution.incrementProcessSkipCount();
            }
            return RepeatStatus.FINISHED;
        };
    }

}
