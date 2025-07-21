package com.sprint.deokhugam.domain.poweruser.scheduler;

import com.sprint.deokhugam.domain.poweruser.service.PowerUserBatchService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class PowerUserScheduler {

    private final PowerUserBatchService powerUserBatchService;
    private final MeterRegistry meterRegistry;

    // 중복 실행 방지를 위한 플래그
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * 매일 새벽 2시에 파워 유저 데이터 계산
     */
//    @Scheduled(cron = "0 * * * * *")
    @Scheduled(cron = "0 0 2 * * *")
    public void schedulePowerUserCalculation() {
        // 중복 실행 체크
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("이전 파워 유저 배치 작업이 아직 실행 중입니다. 스킵합니다.");
            meterRegistry.counter("poweruser.batch.skipped").increment();
            return;
        }

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            log.info("파워 유저 배치 작업 시작");
            int successCount = 0;
            int failureCount = 0;

            // 1. 일간 파워 유저 계산
            try {
                powerUserBatchService.calculateDailyPowerUsers();
                successCount++;
                log.info("일간 파워 유저 계산 완료");
            } catch (Exception e) {
                failureCount++;
                log.error("일간 파워 유저 계산 실패", e);
                meterRegistry.counter("poweruser.batch.daily.failure").increment();
            }

            // 2. 주간 파워 유저 계산 ( 일요일에만 )
            if (isWeeklyCalculationDay()) {
                try {
                    powerUserBatchService.calculateWeeklyPowerUsers();
                    successCount++;
                    log.info("주간 파워 유저 계산 완료");
                } catch (Exception e) {
                    failureCount++;
                    log.error("주간 파워 유저 계산 실패", e);
                    meterRegistry.counter("poweruser.batch.weekly.failure").increment();
                }
            }

            // 3. 월간 파워 유저 계산 ( 매월 1일에만 )
            if (isMonthlyCalculationDay()) {
                try {
                    powerUserBatchService.calculateMonthlyPowerUsers();
                    successCount++;
                    log.info("월간 파워 유저 계산 완료");
                } catch (Exception e) {
                    failureCount++;
                    log.error("월간 파워 유저 계산 실패", e);
                    meterRegistry.counter("poweruser.batch.monthly.failure").increment();
                }
            }

            // 4. 역대 파워 유저 계산 ( 매일 )
            try {
                powerUserBatchService.calculateAllTimePowerUsers();
                successCount++;
                log.info("역대 파워 유저 계산 완료");
            } catch (Exception e) {
                failureCount++;
                log.error("역대 파워 유저 계산 실패", e);
                meterRegistry.counter("poweruser.batch.alltime.failure").increment();
            }

            // 전체 결과 기록
            if (failureCount == 0) {
                meterRegistry.counter("poweruser.batch.success").increment();
                log.info("파워 유저 배치 작업 완료 - 성공: {}, 실패: {}", successCount, failureCount);
            } else {
                meterRegistry.counter("poweruser.batch.partial_failure").increment();
                log.warn("파워 유저 배치 작업 부분 완료 - 성공: {}, 실패: {}", successCount, failureCount);
            }

        } catch (Exception e) {
            meterRegistry.counter("poweruser.batch.total_failure").increment();
            log.error("파워 유저 배치 작업 전체 실패", e);
        } finally {
            isRunning.set(false);
            sample.stop(Timer.builder("poweruser.batch.duration")
                .description("PowerUser batch processing time")
                .register(meterRegistry));
        }
    }

    /**
     * 주간 계산 실행일인지 확인 ( 일요일 )
     */
    private boolean isWeeklyCalculationDay() {
        return LocalDate.now().getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /**
     * 월간 계산 실행일인지 확인 ( 매월 1일 )
     */
    private boolean isMonthlyCalculationDay() {
        return LocalDate.now().getDayOfMonth() == 1;
    }

    /**
     * 현재 배치 작업 실행 상태 확인
     */
    public boolean isBatchRunning() {
        return isRunning.get();
    }
}
