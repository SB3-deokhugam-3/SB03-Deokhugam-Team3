package com.sprint.deokhugam.domain.poweruser.service;

import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.time.LocalDateTime;
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateDailyPowerUsers() {
        log.info("일간 파워 유저 계산 시작");

        try {
            // 어제 00:00:00 ~ 23:59:59
            LocalDateTime endTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime startTime = endTime.minusDays(1);

            Instant startInstant = startTime.toInstant(ZoneOffset.UTC);
            Instant endInstant = endTime.toInstant(ZoneOffset.UTC);

            // 기존 일간 데이터 삭제
            powerUserRepository.deleteByPeriod(PeriodType.DAILY);
            log.debug("기존 일간 파워 유저 데이터 삭제 완료");

            // 새로운 일간 파워 유저 계산
            List<PowerUser> powerUsers = powerUserRepository.calculateAndCreatePowerUsers(
                PeriodType.DAILY, startInstant, endInstant);

            // 배치 저장
            if (!powerUsers.isEmpty()) {
                powerUserService.savePowerUsers(powerUsers);
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
     * 주간 파워 유저 계산 및 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateWeeklyPowerUsers() {
        log.info("주간 파워 유저 계산 시작");

        try {
            // 지난 주 월요일 00:00:00 ~ 일요일 23:59:59
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime startTime = endTime.minusWeeks(1);

            Instant startInstant = startTime.toInstant(ZoneOffset.UTC);
            Instant endInstant = endTime.toInstant(ZoneOffset.UTC);

            // 기존 주간 데이터는 삭제하지 않고 새로운 데이터 추가
            List<PowerUser> powerUsers = powerUserRepository.calculateAndCreatePowerUsers(
                PeriodType.WEEKLY, startInstant, endInstant);

            if (!powerUsers.isEmpty()) {
                powerUserService.savePowerUsers(powerUsers);
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
     * 월간 파워 유저 계산 및 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateMonthlyPowerUsers() {
        log.info("월간 파워 유저 계산 시작");

        try {
            // 지난 달 1일 00:00:00 ~ 마지막 날 23:59:59
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime startTime = endTime.minusMonths(1);

            Instant startInstant = startTime.toInstant(ZoneOffset.UTC);
            Instant endInstant = endTime.toInstant(ZoneOffset.UTC);

            List<PowerUser> powerUsers = powerUserRepository.calculateAndCreatePowerUsers(
                PeriodType.MONTHLY, startInstant, endInstant);

            if (!powerUsers.isEmpty()) {
                powerUserService.savePowerUsers(powerUsers);
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
            // 전체 기간 ( 시작/종료 시간 없음 )
            List<PowerUser> powerUsers = powerUserRepository.calculateAndCreatePowerUsers(
                PeriodType.ALL_TIME, null, null);

            if (!powerUsers.isEmpty()) {
                // 기존 ALL_TIME 데이터 삭제 후 새로운 데이터 저장
                powerUserRepository.deleteByPeriod(PeriodType.ALL_TIME);
                powerUserService.savePowerUsers(powerUsers);
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
