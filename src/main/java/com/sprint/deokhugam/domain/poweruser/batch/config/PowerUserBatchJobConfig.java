package com.sprint.deokhugam.domain.poweruser.batch.config;

import com.sprint.deokhugam.domain.poweruser.service.PowerUserBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PowerUserBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PowerUserBatchService powerUserBatchService;

    @Bean
    public Job powerUserJob() {
        return new JobBuilder("powerUserJob", jobRepository)
            .start(powerUserCalculationStep())
            .build();
    }

    @Bean
    public Step powerUserCalculationStep() {
        return new StepBuilder("powerUserCalculationStep", jobRepository)
            .tasklet(powerUserTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet powerUserTasklet() {
        return (contribution, chunkContext) -> {
            String period = chunkContext.getStepContext()
                .getJobParameters()
                .get("period")
                .toString();

            log.info("PowerUser 배치 작업 실행 - 기간: {}", period);

            switch (period.toUpperCase()) {
                case "DAILY":
                    powerUserBatchService.calculateDailyPowerUsers();
                    break;
                case "WEEKLY":
                    powerUserBatchService.calculateWeeklyPowerUsers();
                    break;
                case "MONTHLY":
                    powerUserBatchService.calculateMonthlyPowerUsers();
                    break;
                case "ALL_TIME":
                    powerUserBatchService.calculateAllTimePowerUsers();
                    break;
                default:
                    throw new IllegalArgumentException("지원하지 않는 기간입니다: " + period);
            }

            log.info("PowerUser 배치 작업 완료 - 기간: {}", period);
            return RepeatStatus.FINISHED;
        };
    }
}
