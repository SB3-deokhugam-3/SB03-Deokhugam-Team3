package com.sprint.deokhugam.domain.poweruser.batch.config;

import com.sprint.deokhugam.domain.poweruser.batch.processor.PowerUserProcessor;
import com.sprint.deokhugam.domain.poweruser.batch.reader.PowerUserDataReader;
import com.sprint.deokhugam.domain.poweruser.batch.writer.PowerUserWriter;
import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.global.enums.PeriodType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PowerUserBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PowerUserDataReader powerUserDataReader;
    private final PowerUserProcessor powerUserProcessor;
    private final PowerUserWriter powerUserWriter;

    @Bean
    public Job powerUserJob() {
        return new JobBuilder("powerUserJob", jobRepository)
            .start(dailyPowerUserStep())
            .next(weeklyPowerUserStep())
            .next(monthlyPowerUserStep())
            .next(allTimePowerUserStep())
            .build();
    }

    @Bean
    public Step dailyPowerUserStep() {
        return new StepBuilder("dailyPowerUserStep", jobRepository)
            .<PowerUserData, PowerUser>chunk(100, transactionManager)
            .reader(createPeriodReader(PeriodType.DAILY))
            .processor(powerUserProcessor)
            .writer(powerUserWriter)
            .build();
    }

    @Bean
    public Step weeklyPowerUserStep() {
        return new StepBuilder("weeklyPowerUserStep", jobRepository)
            .<PowerUserData, PowerUser>chunk(100, transactionManager)
            .reader(createPeriodReader(PeriodType.WEEKLY))
            .processor(powerUserProcessor)
            .writer(powerUserWriter)
            .build();
    }

    @Bean
    public Step monthlyPowerUserStep() {
        return new StepBuilder("monthlyPowerUserStep", jobRepository)
            .<PowerUserData, PowerUser>chunk(100, transactionManager)
            .reader(createPeriodReader(PeriodType.MONTHLY))
            .processor(powerUserProcessor)
            .writer(powerUserWriter)
            .build();
    }

    @Bean
    public Step allTimePowerUserStep() {
        return new StepBuilder("allTimePowerUserStep", jobRepository)
            .<PowerUserData, PowerUser>chunk(100, transactionManager)
            .reader(createPeriodReader(PeriodType.ALL_TIME))
            .processor(powerUserProcessor)
            .writer(powerUserWriter)
            .build();
    }

    private PowerUserDataReader createPeriodReader(PeriodType period) {
        return PowerUserDataReader.createForPeriod(
            powerUserDataReader.getPowerUserRepository(),
            period
        );
    }
}