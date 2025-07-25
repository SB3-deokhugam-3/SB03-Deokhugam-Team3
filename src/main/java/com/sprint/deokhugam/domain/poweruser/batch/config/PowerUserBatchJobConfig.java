package com.sprint.deokhugam.domain.poweruser.batch.config;

import com.sprint.deokhugam.domain.poweruser.batch.processor.PowerUserProcessor;
import com.sprint.deokhugam.domain.poweruser.batch.reader.PowerUserDataReader;
import com.sprint.deokhugam.domain.poweruser.batch.writer.PowerUserWriter;
import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
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
    private final PowerUserRepository powerUserRepository;
    private final PowerUserProcessor powerUserProcessor;
    private final PowerUserWriter powerUserWriter;

    // 단일 Step으로 구성된 Job으로 변경 (매개변수 기반으로 실행할 기간 결정)
    @Bean
    public Job powerUserJob() {
        return new JobBuilder("powerUserJob", jobRepository)
            .start(powerUserStep())
            .build();
    }

    @Bean
    public Step powerUserStep() {
        return new StepBuilder("powerUserStep", jobRepository)
            .<PowerUserData, PowerUser>chunk(100, transactionManager)
            .reader(powerUserDataReader())
            .processor(powerUserProcessor)
            .writer(powerUserWriter)
            .build();
    }

    @Bean
    public PowerUserDataReader powerUserDataReader() {
        // 기본적으로 DAILY로 설정하되, 런타임에 변경될 수 있도록
        PowerUserDataReader reader = new PowerUserDataReader(powerUserRepository);
        reader.setPeriod(PeriodType.DAILY);
        log.info("PowerUserDataReader Bean 생성됨 (기본: DAILY)");
        return reader;
    }

    // 각 기간별 개별 Job 생성
    @Bean
    public Job dailyPowerUserJob() {
        return new JobBuilder("dailyPowerUserJob", jobRepository)
            .start(dailyPowerUserStep())
            .build();
    }

    @Bean
    public Job weeklyPowerUserJob() {
        return new JobBuilder("weeklyPowerUserJob", jobRepository)
            .start(weeklyPowerUserStep())
            .build();
    }

    @Bean
    public Job monthlyPowerUserJob() {
        return new JobBuilder("monthlyPowerUserJob", jobRepository)
            .start(monthlyPowerUserStep())
            .build();
    }

    @Bean
    public Job allTimePowerUserJob() {
        return new JobBuilder("allTimePowerUserJob", jobRepository)
            .start(allTimePowerUserStep())
            .build();
    }

    @Bean
    public Step dailyPowerUserStep() {
        return new StepBuilder("dailyPowerUserStep", jobRepository)
            .<PowerUserData, PowerUser>chunk(100, transactionManager)
            .reader(dailyPowerUserDataReader())
            .processor(powerUserProcessor)
            .writer(powerUserWriter)
            .build();
    }

    @Bean
    public Step weeklyPowerUserStep() {
        return new StepBuilder("weeklyPowerUserStep", jobRepository)
            .<PowerUserData, PowerUser>chunk(100, transactionManager)
            .reader(weeklyPowerUserDataReader())
            .processor(powerUserProcessor)
            .writer(powerUserWriter)
            .build();
    }

    @Bean
    public Step monthlyPowerUserStep() {
        return new StepBuilder("monthlyPowerUserStep", jobRepository)
            .<PowerUserData, PowerUser>chunk(100, transactionManager)
            .reader(monthlyPowerUserDataReader())
            .processor(powerUserProcessor)
            .writer(powerUserWriter)
            .build();
    }

    @Bean
    public Step allTimePowerUserStep() {
        return new StepBuilder("allTimePowerUserStep", jobRepository)
            .<PowerUserData, PowerUser>chunk(100, transactionManager)
            .reader(allTimePowerUserDataReader())
            .processor(powerUserProcessor)
            .writer(powerUserWriter)
            .build();
    }

    @Bean
    public PowerUserDataReader dailyPowerUserDataReader() {
        PowerUserDataReader reader = PowerUserDataReader.createForPeriod(powerUserRepository, PeriodType.DAILY);
        log.info("Daily PowerUserDataReader Bean 생성됨");
        return reader;
    }

    @Bean
    public PowerUserDataReader weeklyPowerUserDataReader() {
        PowerUserDataReader reader = PowerUserDataReader.createForPeriod(powerUserRepository, PeriodType.WEEKLY);
        log.info("Weekly PowerUserDataReader Bean 생성됨");
        return reader;
    }

    @Bean
    public PowerUserDataReader monthlyPowerUserDataReader() {
        PowerUserDataReader reader = PowerUserDataReader.createForPeriod(powerUserRepository, PeriodType.MONTHLY);
        log.info("Monthly PowerUserDataReader Bean 생성됨");
        return reader;
    }

    @Bean
    public PowerUserDataReader allTimePowerUserDataReader() {
        PowerUserDataReader reader = PowerUserDataReader.createForPeriod(powerUserRepository, PeriodType.ALL_TIME);
        log.info("AllTime PowerUserDataReader Bean 생성됨");
        return reader;
    }
}