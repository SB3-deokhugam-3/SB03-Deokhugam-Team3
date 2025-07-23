package com.sprint.deokhugam.domain.notification.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.deokhugam.domain.notification.entity.Notification;
import com.sprint.deokhugam.domain.notification.repository.NotificationRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) // 이 테스트 후 컨텍스트 재생성
@ActiveProfiles("test")
class NotificationDeleteJobConfigTest {

    @MockitoBean
    private com.sprint.deokhugam.domain.book.ocr.TesseractOcrExtractor tesseractOcrExtractor;

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job notificationDeleteJob;
    @Autowired
    private NotificationTestDataHelper testDataHelper;

    @Test
    void 배치_1주일_이상_지난_알림_삭제_테스트() throws Exception {
        // given
        testDataHelper.saveTestData();

        // when
        JobExecution execution = jobLauncher.run(
            notificationDeleteJob,
            new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters()
        );

        // then
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        List<Notification> remain = notificationRepository.findAll();
        assertThat(remain).extracting("content")
            .containsExactlyInAnyOrder("recent confirmed", "old unconfirmed");
    }

    @AfterEach
    void tearDown() {
        testDataHelper.clearTestData();
    }
}