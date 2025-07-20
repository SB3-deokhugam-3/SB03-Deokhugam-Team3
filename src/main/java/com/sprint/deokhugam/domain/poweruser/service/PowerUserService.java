package com.sprint.deokhugam.domain.poweruser.service;

import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PowerUserService {

    private final PowerUserRepository powerUserRepository;

    /**
     * 활동 점수 계산
     * 점수 = ( 리뷰 인기 점수 * 0.5 ) + ( 좋아요 수 * 0.2 ) + ( 댓글 수 * 0.3 )
     * */
    public Double calculateActivityScore(Double reviewScoreSum, Long likeCount, Long commentCount) {
        return (reviewScoreSum * 0.5) + (likeCount * 0.2) + (commentCount * 0.3);
    }

    /**
     * 기간별 파워 유저 데이터 저장
     * */
    public void  savePowerUsers(List<PowerUser> powerUsers) {
        // 기존 데이터 삭제 후 새로운 데이터 저장
        if (!powerUsers.isEmpty()) {
            PeriodType period = powerUsers.get(0).getPeriod();
            powerUserRepository.deleteByPeriod(period);

            // 순위 설정
            for (int i = 0; i < powerUsers.size(); i++) {
                powerUsers.get(i).setRank((long) (i + 1));
            }

            powerUserRepository.saveAll(powerUsers);
            log.info("파워 유저 데이터 저장 완료: {} 기간, {} 명", period, powerUsers.size());
        }
    }
    /**
     * 기간별 파워 유저 조회 ( 상위 10 명 )
     * */
    public List<PowerUser> getPowerUsersByPeriod(PeriodType period, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return powerUserRepository.findByPeriodOrderByRankAsc(period,pageable);
    }


}
