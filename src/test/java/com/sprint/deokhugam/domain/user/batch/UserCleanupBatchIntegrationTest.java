package com.sprint.deokhugam.domain.user.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@SpringBatchTest
@TestPropertySource(properties = {
    "spring.batch.job.enabled=false", // 자동 실행 방지
    "logging.level.com.sprint.deokhugam.domain.user.batch=DEBUG"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) // 이 테스트 후 컨텍스트 재생성
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@ActiveProfiles("test")
class UserCleanupBatchIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private Job userCleanupJob;

    @Autowired
    private UserCleanupTestDataHelper testDataHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        jobRepositoryTestUtils.removeJobExecutions();
        jobLauncherTestUtils.setJob(userCleanupJob);
        testDataHelper.saveTestData();
    }

    @AfterEach
    void tearDown() {
        testDataHelper.clearTestData();
    }

    @Test
    @DisplayName("배치 삭제 대상 유저 확인")
    void checkDeletableUsers() {
        // Given
        Instant cutoff = Instant.now().minus(1, ChronoUnit.DAYS);

        // When
        List<User> deletableUsers = userRepository.findDeletableUsers(cutoff);

        // Then
        System.out.println("삭제 대상 유저 수: " + deletableUsers.size());
        deletableUsers.forEach(user -> {
            System.out.println("유저 ID: " + user.getId() +
                ", 이메일: " + user.getEmail() +
                ", 삭제됨: " + user.getIsDeleted() +
                ", 삭제시간: " + user.getDeletedAt());
        });

        assertThat(deletableUsers).hasSize(1);
    }

    @Test
    void 유저가_탈퇴하고_하루가_지나면_관련_데이터가_모두_삭제된다() throws Exception {
        // Given
        UserCleanupTestDataHelper.TestDataCounts beforeCounts = testDataHelper.getDataCounts();

        assertThat(beforeCounts.userCount).isEqualTo(3); // 삭제대상1, 최근삭제1, 활성1 (네이티브 쿼리로 모든 유저 조회)
        assertThat(beforeCounts.reviewCount).isEqualTo(3);
        assertThat(beforeCounts.commentCount).isEqualTo(2);
        assertThat(beforeCounts.notificationCount).isEqualTo(2);
        assertThat(beforeCounts.reviewLikeCount).isEqualTo(2);
        assertThat(beforeCounts.bookCount).isEqualTo(1);

        // When: 배치 실행
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then: 배치 실행 결과 검증
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 삭제 후 데이터 개수 확인
        UserCleanupTestDataHelper.TestDataCounts afterCounts = testDataHelper.getDataCounts();

        assertThat(afterCounts.userCount).isEqualTo(2); // 하루 이상 지난 삭제 유저 1명이 제거되어 2명 남음
        assertThat(afterCounts.reviewCount).isEqualTo(2); // 삭제된 유저의 리뷰 1개 제거되어 2개 남음
        assertThat(afterCounts.commentCount).isEqualTo(1); // 삭제된 유저의 댓글 1개 제거되어 1개 남음
        assertThat(afterCounts.notificationCount).isEqualTo(1); // 삭제된 유저의 알림 1개 제거되어 1개 남음
        assertThat(afterCounts.reviewLikeCount).isEqualTo(1); // 삭제된 유저의 좋아요 1개 제거되어 1개 남음
        assertThat(afterCounts.bookCount).isEqualTo(1); // 책은 삭제되지 않음

        UserCleanupTestDataHelper.UserTypeCounts userTypeCounts = testDataHelper.getUserTypeCounts();
        assertThat(userTypeCounts.activeUserCount).isEqualTo(1); // 활성 유저 1명
        assertThat(userTypeCounts.deletedUserCount).isEqualTo(1); // 최근 삭제 유저 1명 (아직 24시간 안됨)

        // 책의 리뷰 카운트
        bookRepository.findAll().forEach(book -> {
            long actualReviewCount = 2; // 최근삭제유저 + 활성유저 리뷰
            assertThat(book.getReviewCount()).isEqualTo(actualReviewCount);
        });
    }

    @Test
    @DisplayName("삭제 대상이 없을 때 배치가 정상 종료되는지 테스트")
    void 삭제_대상이_없으면_배치가_정상_종료_된다() throws Exception {
        // Given
        testDataHelper.clearTestData();

        // When
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }
}