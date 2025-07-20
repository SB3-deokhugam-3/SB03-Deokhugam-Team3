package com.sprint.deokhugam.domain.poweruser.repository;

import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PowerUserRepository extends JpaRepository<PowerUser, UUID>, PowerUserRepositoryCustom{

    /**
     * 기간별 순위 조회 (페이징)
     * */
    List<PowerUser> findByPeriodOrderByRankAsc(PeriodType period, Pageable pageable);

    /**
     *  특정 사용자의 기간별 파워유저 정보 조회
     *  */
    Optional<PowerUser> findByUserIdAndPeriod(UUID userId, PeriodType period);

    /**
     *  기간별 파워유저 존재 여부 확인
     *  */
    boolean existsByUserIdAndPeriod(UUID userId, PeriodType period);

    /**
     *  기간별 총 파워유저 수 조회
     *  */
    Long countByPeriod(PeriodType period);

    /**
     *  특정 순위 범위 내의 파워유저 조회
     *  */
    @Query("SELECT p FROM PowerUser p WHERE p.period = :period AND p.rank BETWEEN :startRank AND :endRank ORDER BY p.rank ASC")
    List<PowerUser> findByPeriodAndRankBetween(@Param("period") PeriodType period,
        @Param("startRank") Long startRank,
        @Param("endRank") Long endRank);

    /**
     * 커서 기반 파워유저 조회
     */
    List<PowerUser> findPowerUsersWithCursor(PeriodType period, String direction, int limit, String cursor, String after);

    /**
     *  기간별 삭제
     *  */
    void deleteByPeriod(PeriodType period);

}
