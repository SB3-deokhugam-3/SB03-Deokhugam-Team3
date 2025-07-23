package com.sprint.deokhugam.config;

import com.sprint.deokhugam.domain.notification.batch.NotificationDeleteJobConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(NotificationDeleteJobConfig.class)
public class TestBatchConfig {

}