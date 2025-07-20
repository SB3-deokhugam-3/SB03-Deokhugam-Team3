package com.sprint.deokhugam.domain.poweruser.scheduler;

import com.sprint.deokhugam.domain.poweruser.service.PowerUserBatchService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PowerUserScheduler {

    private final PowerUserBatchService powerUserBatchService;

    /**
     * 매일 새벽 2시에 파워 유저 데이터 계산
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void schedulePowerUserCalculation() {
        log.info("파워 유저 배치 작업 시작");

        try {
            // 일간 파워 유저 계산
            powerUserBatchService.calculateDailyPowerUsers();

            // 주간 파워 유저 계산 ( 일요일에만 )
            DayOfWeek today = LocalDate.now().getDayOfWeek();
            if (today == DayOfWeek.SUNDAY) {
                powerUserBatchService.calculateWeeklyPowerUsers();
            }

            // 월간 파워 유저 계산 ( 매월 1일에만 )
            if (LocalDate.now().getDayOfMonth() == 1) {
                powerUserBatchService.calculateMonthlyPowerUsers();
            }

            // 역대 파워 유저는 매일 계산
            powerUserBatchService.calculateAllTimePowerUsers();

            log.info("파워 유저 배치 작업 완료");
        } catch (Exception e) {
            log.error("파워 유저 배치 작업 실패", e);
        }
    }
}
