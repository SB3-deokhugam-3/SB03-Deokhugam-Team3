package com.sprint.deokhugam.domain.popularreview.repository;

import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID>,
    PopularReviewRepositoryCustom {

    Boolean existsByCreatedAtBetween(Instant createdAtAfter, Instant createdAtBefore);

    /**
     * 특정 사용자의 기간별 인기 리뷰 점수 합계 조회
     */
    @Query("""
        SELECT COALESCE(SUM(pr.score), 0.0)
        FROM PopularReview pr
        JOIN pr.review r
        WHERE r.user.id = :userId 
        AND pr.period = :period
        """)
    Double findScoreSumByUserIdAndPeriod(@Param("userId") UUID userId, @Param("period") PeriodType period);
}
