package com.sprint.deokhugam.domain.popularbook.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sprint.deokhugam.domain.popularbook.entity.PopularBook;
import com.sprint.deokhugam.domain.popularbook.repository.PopularBookRepository;
import com.sprint.deokhugam.global.period.PeriodType;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@Sql("/sql/popular-book-ranking-test-data.sql")
class PopularBookRankingBatchJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job popularBookRankingJob;

    @Autowired
    private PopularBookRepository popularBookRepository;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(popularBookRankingJob);
    }

    @ParameterizedTest
    @EnumSource(PeriodType.class)
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    void 기간_별_인기_도서_생성_확인_테스트(PeriodType period) throws Exception {

        // given
        String today = ZonedDateTime.of(2025, 7, 19,
                0, 0, 0, 0,
                ZoneId.of("Asia/Seoul"))
            .toInstant()
            .toString();

        JobParameters jobParameters = new JobParametersBuilder()
            .addString("period", period.name())
            .addString("today", today)
            .addLong("time", System.currentTimeMillis()) // 유니크 보장
            .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        List<PopularBook> results = popularBookRepository.findAll();
        assertEquals(1L, results.get(0).getRank());
    }
}