package com.sprint.deokhugam.domain.poweruser.batch.processor;

import com.sprint.deokhugam.domain.popularreview.service.PopularReviewService;
import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.service.PowerUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PowerUserProcessor implements ItemProcessor<PowerUserData, PowerUser> {

    private final PopularReviewService popularReviewService;

    @Override
    public PowerUser process(PowerUserData userData) throws Exception {
        if (userData == null) {
            return null;
        }

        log.debug("PowerUser 처리 시작: {}", userData.getUserSummary());

        // 이미 Reader에서 조회한 데이터를 그대로 사용 (중복 조회 제거)
        Double reviewScoreSum = userData.reviewScoreSum();
        Long likeCount = userData.likeCount();
        Long commentCount = userData.commentCount();

        // 총 활동 점수 계산
        Double totalScore = PowerUserService.calculateActivityScore(
            reviewScoreSum, likeCount, commentCount);

        log.debug("점수 계산 완료 - userId: {}, 인기리뷰점수: {}, 좋아요: {}, 댓글: {}, 총점: {}",
            userData.user().getId(), reviewScoreSum, likeCount, commentCount, totalScore);

        // PowerUser 객체 생성
        return PowerUser.builder()
            .user(userData.user())
            .period(userData.period())
            .rank(1L) // 임시 순위 (Writer에서 재정렬됨)
            .score(totalScore)
            .reviewScoreSum(reviewScoreSum) // 실제 인기 리뷰 점수 저장
            .likeCount(likeCount)
            .commentCount(commentCount)
            .build();
    }
}