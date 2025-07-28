package com.sprint.deokhugam.domain.popularreview.batch;

import com.sprint.deokhugam.domain.comment.repository.CommentRepository;
import com.sprint.deokhugam.domain.popularreview.service.PopularReviewService;
import com.sprint.deokhugam.domain.reviewlike.repository.ReviewLikeRepository;
import com.sprint.deokhugam.global.batch.BatchListener;
import com.sprint.deokhugam.global.enums.PeriodType;
import com.sprint.deokhugam.global.exception.BatchAlreadyRunException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
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
public class ReviewBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchListener listener;

    private final PopularReviewService popularReviewService;
    private final CommentRepository commentRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    @Bean
    public Job popularReviewJob() {
        return new JobBuilder("popularReviewJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(popularReviewStep(jobRepository, transactionManager))
            .build();
    }

    @Bean
    public Step popularReviewStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("reviewStep", jobRepository)
            .tasklet(popularReviewTasklet(), transactionManager)
            .listener(listener)
            .build();
    }

    protected Tasklet popularReviewTasklet() {
        return (stepContribution, chunkContext) -> {
            try {
                Instant currentTime = Instant.now();
                /* 오늘 이미 실행한 배치인지 검증 */
                popularReviewService.validateJobNotDuplicated(currentTime);

                for (PeriodType period : PeriodType.values()) {
                    ZoneId zoneId = ZoneId.of("Asia/Seoul");
                    Instant start = period.getStartInstant(currentTime, zoneId);
                    Instant end = period.getEndInstant(currentTime, zoneId);

                    // 특정 기간 동안 추가된 댓글 / 좋아요 수
                    Map<UUID, Long> commentMap = commentRepository.countByReviewIdBetween(start,
                        end);
                    Map<UUID, Long> likeMap = reviewLikeRepository.countByReviewIdBetween(start,
                        end);

                    popularReviewService.savePopularReviewsByPeriod(
                        period, currentTime, commentMap, likeMap, stepContribution
                    );
                }
            } catch (BatchAlreadyRunException e) {
                log.error(e.getMessage(), e);
                stepContribution.incrementProcessSkipCount();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                stepContribution.incrementProcessSkipCount();
                throw e;
            }
            return RepeatStatus.FINISHED;
        };
    }
}
