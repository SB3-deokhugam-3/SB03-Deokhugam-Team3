package com.sprint.deokhugam.domain.popularbook.config;

import com.sprint.deokhugam.domain.popularbook.dto.data.BookScoreDto;
import com.sprint.deokhugam.domain.popularbook.entity.PopularBook;
import com.sprint.deokhugam.domain.popularbook.exception.MissingPeriodParameterException;
import com.sprint.deokhugam.domain.popularbook.processor.BookScoreProcessor;
import com.sprint.deokhugam.domain.popularbook.reader.JpaBookScoreReader;
import com.sprint.deokhugam.domain.popularbook.repository.PopularBookRepository;
import com.sprint.deokhugam.domain.popularbook.tasklet.DeletePopularBooksTasklet;
import com.sprint.deokhugam.domain.popularbook.writer.PopularBookRankingWriter;
import com.sprint.deokhugam.global.enums.PeriodType;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class PopularBookRankingBatchJobConfig {

    private final EntityManager em;
    private final PopularBookRepository popularBookRepository;

    @Bean
    public Job popularBookRankingJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {

        return new JobBuilder("popularBookRankingJob", jobRepository)
            .start(deletePopularBookStep(jobRepository, transactionManager, ""))
            .next(popularBookRankingStep(jobRepository, transactionManager, "", ""))
            .build();
    }

    @Bean
    @JobScope
    public Step deletePopularBookStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        @Value("#{jobParameters['period']}") String periodKey) {

        if (periodKey == null) {
            throw new MissingPeriodParameterException("period 파라미터는 필수입니다.");
        }

        PeriodType period = PeriodType.valueOf(periodKey.toUpperCase());

        return new StepBuilder("deletePopularBookStep_" + period.name(), jobRepository)
            .tasklet(new DeletePopularBooksTasklet(popularBookRepository, period), transactionManager)
            .build();
    }

    @Bean
    @JobScope
    public Step popularBookRankingStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        @Value("#{jobParameters['period']}") String periodKey,
        @Value("#{jobParameters['today']}") String todayStr) {

        if (periodKey == null || todayStr == null) {
            throw new MissingPeriodParameterException("period와 today 파라미터는 필수입니다.");
        }

        PeriodType period = PeriodType.valueOf(periodKey.toUpperCase());
        Instant today = Instant.parse(todayStr);

        List<BookScoreDto> results = new JpaBookScoreReader(em, period, today).readAll();
        ItemReader<BookScoreDto> reader = new IteratorItemReader<>(results);

        ItemProcessor<BookScoreDto, PopularBook> processor = new BookScoreProcessor(em, period);
        ItemWriter<PopularBook> writer = new PopularBookRankingWriter(popularBookRepository);

        return new StepBuilder("popularBookStep_" + period.name(), jobRepository)
            .<BookScoreDto, PopularBook>chunk(100, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }
}
