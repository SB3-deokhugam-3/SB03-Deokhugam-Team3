package com.sprint.deokhugam.domain.poweruser.controller;

import com.sprint.deokhugam.domain.poweruser.service.PowerUserBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/powerusers")
@RequiredArgsConstructor
public class PowerUserAdminController {

    private final JobLauncher jobLauncher;
    private final Job powerUserJob;
    private final PowerUserBatchService powerUserBatchService;

    @PostMapping("/batch/run")
    public ResponseEntity<String> runPowerUserBatch() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(powerUserJob, jobParameters);

            return ResponseEntity.ok("배치 실행 시작: " + jobExecution.getStatus());
        } catch (Exception e) {
            log.error("배치 실행 실패", e);
            return ResponseEntity.status(500).body("배치 실행 실패: " + e.getMessage());
        }
    }

    @PostMapping("/calculate/daily")
    public ResponseEntity<String> calculateDaily() {
        try {
            powerUserBatchService.calculateDailyPowerUsers();
            return ResponseEntity.ok("일간 파워유저 계산 완료");
        } catch (Exception e) {
            log.error("일간 계산 실패", e);
            return ResponseEntity.status(500).body("계산 실패: " + e.getMessage());
        }
    }
}
