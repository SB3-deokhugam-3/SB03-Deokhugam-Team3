package com.sprint.deokhugam.domain.popularbook.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.popularbook.entity.PopularBook;
import com.sprint.deokhugam.domain.popularbook.repository.PopularBookRepository;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
@Sql("/sql/popular-book-ranking-test-data.sql")
class PopularBookRankingBatchJobConfigTest {

    @TestConfiguration
    static class BatchTestConfig {

        @Bean
        public JobLauncherTestUtils jobLauncherTestUtils() {
            return new JobLauncherTestUtils();
        }
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job popularBookRankingJob;

    @Autowired
    private PopularBookRepository popularBookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(popularBookRankingJob);
    }

    @Test
    void 일간_인기_도서_생성_확인_테스트() throws Exception {

        // given
        String period = "DAILY";
        String today = "2025-07-20T00:00:00Z";

        JobParameters jobParameters = new JobParametersBuilder()
            .addString("period", period)
            .addString("today", today)
            .addLong("time", System.currentTimeMillis()) // 유니크 보장
            .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        List<PopularBook> results = popularBookRepository.findAll();
        assertEquals(2, results.size()); // 예상 결과: 2개의 랭킹 데이터
    }
}