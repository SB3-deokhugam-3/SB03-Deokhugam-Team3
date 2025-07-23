package com.sprint.deokhugam.domain.popularreview.repository;

import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PopularReviewRepository extends JpaRepository<PopularReview, UUID>,
    PopularReviewRepositoryCustom {

//    // 특정 기간 데이터 삭제(배치 초기화용)
//    void deleteByPeriod(PeriodType period);

}
