package com.sprint.deokhugam.domain.poweruser.controller;

import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.domain.poweruser.service.PowerUserBatchService;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/poweruser")
@RequiredArgsConstructor
@Slf4j
public class PowerUserAdminController {

    private final JobLauncher jobLauncher;
    private final Job powerUserJob;
    private final PowerUserBatchService powerUserBatchService;
    private final PowerUserRepository powerUserRepository;

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
            log.error("일간 파워유저 계산 실패", e);
            return ResponseEntity.status(500).body("실행 실패: " + e.getMessage());
        }
    }

    @PostMapping("/calculate/weekly")
    public ResponseEntity<String> calculateWeekly() {
        try {
            powerUserBatchService.calculateWeeklyPowerUsers();
            return ResponseEntity.ok("주간 파워유저 계산 완료");
        } catch (Exception e) {
            log.error("주간 파워유저 계산 실패", e);
            return ResponseEntity.status(500).body("실행 실패: " + e.getMessage());
        }
    }

    @PostMapping("/calculate/monthly")
    public ResponseEntity<String> calculateMonthly() {
        try {
            powerUserBatchService.calculateMonthlyPowerUsers();
            return ResponseEntity.ok("월간 파워유저 계산 완료");
        } catch (Exception e) {
            log.error("월간 파워유저 계산 실패", e);
            return ResponseEntity.status(500).body("실행 실패: " + e.getMessage());
        }
    }

    @PostMapping("/calculate/all-time")
    public ResponseEntity<String> calculateAllTime() {
        try {
            powerUserBatchService.calculateAllTimePowerUsers();
            return ResponseEntity.ok("역대 파워유저 계산 완료");
        } catch (Exception e) {
            log.error("역대 파워유저 계산 실패", e);
            return ResponseEntity.status(500).body("실행 실패: " + e.getMessage());
        }
    }

    @GetMapping("/check/{period}")
    public ResponseEntity<Map<String, Object>> checkData(@PathVariable PeriodType period) {
        Map<String, Object> result = new HashMap<>();

        long count = powerUserRepository.countByPeriod(period);
        List<PowerUser> topUsers = powerUserRepository.findTopPowerUsersNByPeriod(period, 5);

        result.put("count", count);
        result.put("topUsers", topUsers.stream()
            .map(pu -> Map.of(
                "nickname", pu.getUser().getNickname(),
                "rank", pu.getRank(),
                "score", pu.getScore(),
                "likeCount", pu.getLikeCount(),
                "commentCount", pu.getCommentCount()
            ))
            .toList());

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/clear/{period}")
    public ResponseEntity<String> clearData(@PathVariable PeriodType period) {
        try {
            long deletedCount = powerUserRepository.deleteByPeriod(period);
            return ResponseEntity.ok(period + " 데이터 " + deletedCount + "건 삭제 완료");
        } catch (Exception e) {
            log.error("데이터 삭제 실패", e);
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }
}