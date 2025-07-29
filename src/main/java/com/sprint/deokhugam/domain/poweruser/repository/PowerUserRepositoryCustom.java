package com.sprint.deokhugam.domain.poweruser.repository;

import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PowerUserRepositoryCustom {

    /**
     * 배치 작업을 위한 사용자 활동 점수 계산 및 파워유저 생성
     */
    List<PowerUser> calculateAndCreatePowerUsers(PeriodType period, Instant startDate,
        Instant endDate);

    /**
     * 배치 처리를 위한 사용자별 활동 데이터 조회
     */
    List<PowerUserData> findUserActivityData(PeriodType period, Instant startDate, Instant endDate);

    /**
     * 파워유저 순위 재계산 ( 점수 기준으로 순위 업데이트 )
     */
    void recalculateRank(PeriodType period);

    /**
     * 기간별 파워유저 상위 N명 조회 ( with user join )
     */
    List<PowerUser> findTopPowerUsersNByPeriod(PeriodType period, int limit);

    /**
     * 사용자별 모든 기간의 파워유저 이력 조회
     */
    List<PowerUser> findPowerUserHistoryByUserId(UUID userId);

    /**
     * 파워 유저 배치 삽입/업데이트
     */
    void batchUpsertPowerUsers(List<PowerUser> powerUsers);

    /**
     * 커서 기반 파워유저 조회
     */
    List<PowerUser> findPowerUsersWithCursor(PeriodType period, String direction, int limit,
        String cursor, String after);
}