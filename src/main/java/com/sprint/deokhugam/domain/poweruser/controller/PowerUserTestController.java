package com.sprint.deokhugam.domain.poweruser.controller;

import com.sprint.deokhugam.domain.poweruser.service.PowerUserBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/test/power-users")
@Profile({"dev", "test"})
public class PowerUserTestController {

    private final PowerUserBatchService powerUserBatchService;

    /**
     * 테스트용: 모든 기간 파워유저 계산
     */
    @PostMapping("/calculate/all")
    public ResponseEntity<String> calculateAllPowerUsers() {
        try {
            powerUserBatchService.calculateDailyPowerUsers();
            powerUserBatchService.calculateWeeklyPowerUsers();
            powerUserBatchService.calculateMonthlyPowerUsers();
            powerUserBatchService.calculateAllTimePowerUsers();

            return ResponseEntity.ok("모든 기간 파워유저 계산 완료");
        } catch (Exception e) {
            log.error("파워유저 계산 실패", e);
            return ResponseEntity.status(500).body("계산 실패: " + e.getMessage());
        }
    }

    /**
     * 테스트용: 특정 기간 파워유저 계산
     */
    @PostMapping("/calculate/{period}")
    public ResponseEntity<String> calculatePowerUsersByPeriod(@PathVariable String period) {
        try {
            switch (period.toUpperCase()) {
                case "DAILY":
                    powerUserBatchService.calculateDailyPowerUsers();
                    break;
                case "WEEKLY":
                    powerUserBatchService.calculateWeeklyPowerUsers();
                    break;
                case "MONTHLY":
                    powerUserBatchService.calculateMonthlyPowerUsers();
                    break;
                case "ALL_TIME":
                    powerUserBatchService.calculateAllTimePowerUsers();
                    break;
                default:
                    return ResponseEntity.badRequest().body("잘못된 기간: " + period);
            }

            return ResponseEntity.ok(period + " 파워유저 계산 완료");
        } catch (Exception e) {
            log.error("{} 파워유저 계산 실패", period, e);
            return ResponseEntity.status(500).body("계산 실패: " + e.getMessage());
        }
    }
}

