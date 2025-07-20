package com.sprint.deokhugam.domain.poweruser.service;

import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PowerUserBatchService {

    private final PowerUserRepository powerUserRepository;
    private final PowerUserService powerUserService;

    /**
     * 일간 파워 유저 계산 및 저장
     * */
    @Transactional
    public void calculateDailyPowerUsers() {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(1);

        List<PowerUser> dailyPowerUsers = powerUserRepository
            .calculateAndCreatePowerUsers(PeriodType.DAILY,
                startTime.toInstant(ZoneOffset.UTC),
                endTime.toInstant(ZoneOffset.UTC));

        powerUserService.savePowerUsers(dailyPowerUsers);
        log.info("일간 파워 유저 계산 완료 : {} 명", dailyPowerUsers.size());
    }

    /**
     * 주간 파워 유저 계산 및 저장
     * */
    @Transactional
    public void calculateWeeklyPowerUsers() {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusWeeks(1);

        List<PowerUser> weeklyPowerUsers = powerUserRepository
            .calculateAndCreatePowerUsers(PeriodType.WEEKLY,
                startTime.toInstant(ZoneOffset.UTC),
                endTime.toInstant(ZoneOffset.UTC));

        powerUserService.savePowerUsers(weeklyPowerUsers);
        log.info("주간 파워 유저 계싼 완료 : {} 명", weeklyPowerUsers.size());
    }

    /**
     * 월간 파워 유저 계산 및 저장
     * */
    @Transactional
    public void calculateMonthlyPowerUsers() {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMonths(1);

        List<PowerUser> monthlyPowerUsers = powerUserRepository
            .calculateAndCreatePowerUsers(PeriodType.MONTHLY,
                startTime.toInstant(ZoneOffset.UTC),
                endTime.toInstant(ZoneOffset.UTC));

        powerUserService.savePowerUsers(monthlyPowerUsers);
        log.info("월간 파워 유저 계산 완료 : {} 명", monthlyPowerUsers.size());
    }

    /**
     * 역대 파워 유저 계산 및 저장
     * */
    @Transactional
    public void calculateAllTimePowerUsers() {
        List<PowerUser> allTimePowerUsers = powerUserRepository
            .calculateAndCreatePowerUsers(PeriodType.ALL_TIME,null,null);

        powerUserService.savePowerUsers(allTimePowerUsers);
        log.info("역대 파워 유저 계산 완료: {} 명", allTimePowerUsers.size());
    }
}
