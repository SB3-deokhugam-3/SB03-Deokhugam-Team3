package com.sprint.deokhugam.domain.poweruser.service;

import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PowerUserBatchService {

    private final PowerUserRepository powerUserRepository;
    private final PowerUserService powerUserService;

    /**
     * 일간 파워 유저 계산 및 저장
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void calculateDailyPowerUsers() {
        log.info("일간 파워 유저 계산 시작");

        try {
            // 어제 00:00:00 ~ 23:59:59
            LocalDateTime endTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)
                .withNano(0);
            LocalDateTime startTime = endTime.minusDays(1);

            ZoneId koreaZone = ZoneId.of("Asia/Seoul");
            Instant startInstant = startTime.atZone(koreaZone).toInstant();
            Instant endInstant = endTime.atZone(koreaZone).toInstant();

            // 새로운 일간 파워 유저 계산
            List<PowerUser> powerUsers = powerUserRepository.calculateAndCreatePowerUsers(
                PeriodType.DAILY, startInstant, endInstant);

            // 기존 데이터 삭제 후 새로 저장
            if (!powerUsers.isEmpty()) {
                powerUserService.replacePowerUsers(powerUsers); // 삭제 + 저장
                log.info("일간 파워 유저 계산 완료 - 총 {}명", powerUsers.size());
            } else {
                log.info("일간 파워 유저 계산 완료 - 대상 사용자 없음");
            }

        } catch (Exception e) {
            log.error("일간 파워 유저 계산 중 오류 발생", e);
            throw new RuntimeException("일간 파워 유저 계산 실패", e);
        }

    }

    /**
     * 주간 파워 유저 계산 및 저장 (지난 7일간)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateWeeklyPowerUsers() {
        log.info("주간 파워 유저 계산 시작");

        try {
            // 현재 시점에서 7일 전부터 지금까지
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(7);

            Instant startInstant = startTime.toInstant(ZoneOffset.UTC);
            Instant endInstant = endTime.toInstant(ZoneOffset.UTC);

            log.info("주간 파워유저 계산 범위: {} ~ {}", startTime, endTime);

            List<PowerUser> powerUsers = powerUserRepository.calculateAndCreatePowerUsers(
                PeriodType.WEEKLY, startInstant, endInstant);

            if (!powerUsers.isEmpty()) {
                powerUserService.replacePowerUsers(powerUsers);
                log.info("주간 파워 유저 계산 완료 - 총 {}명", powerUsers.size());
            } else {
                log.info("주간 파워 유저 계산 완료 - 대상 사용자 없음");
            }

        } catch (Exception e) {
            log.error("주간 파워 유저 계산 중 오류 발생", e);
            throw new RuntimeException("주간 파워 유저 계산 실패", e);
        }
    }

    /**
     * 월간 파워 유저 계산 및 저장 (지난 30일간)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateMonthlyPowerUsers() {
        log.info("월간 파워 유저 계산 시작");

        try {
            // 현재 시점에서 30일 전부터 지금까지
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(30);

            Instant startInstant = startTime.toInstant(ZoneOffset.UTC);
            Instant endInstant = endTime.toInstant(ZoneOffset.UTC);

            log.info("월간 파워유저 계산 범위: {} ~ {}", startTime, endTime);

            List<PowerUser> powerUsers = powerUserRepository.calculateAndCreatePowerUsers(
                PeriodType.MONTHLY, startInstant, endInstant);

            if (!powerUsers.isEmpty()) {
                powerUserService.replacePowerUsers(powerUsers); // 기존 MONTHLY 삭제 후 새로 저장
                log.info("월간 파워 유저 계산 완료 - 총 {}명", powerUsers.size());
            } else {
                log.info("월간 파워 유저 계산 완료 - 대상 사용자 없음");
            }

        } catch (Exception e) {
            log.error("월간 파워 유저 계산 중 오류 발생", e);
            throw new RuntimeException("월간 파워 유저 계산 실패", e);
        }
    }

    /**
     * 역대 파워 유저 계산 및 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateAllTimePowerUsers() {
        log.info("역대 파워 유저 계산 시작");

        try {
            List<PowerUser> powerUsers = powerUserRepository.calculateAndCreatePowerUsers(
                PeriodType.ALL_TIME, null, null);

            if (!powerUsers.isEmpty()) {
                powerUserService.replacePowerUsers(powerUsers); //  삭제 + 저장
                log.info("역대 파워 유저 계산 완료 - 총 {}명", powerUsers.size());
            } else {
                log.info("역대 파워 유저 계산 완료 - 대상 사용자 없음");
            }

        } catch (Exception e) {
            log.error("역대 파워 유저 계산 중 오류 발생", e);
            throw new RuntimeException("역대 파워 유저 계산 실패", e);
        }
    }
}
