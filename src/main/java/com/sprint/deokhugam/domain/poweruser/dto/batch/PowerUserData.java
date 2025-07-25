package com.sprint.deokhugam.domain.poweruser.dto.batch;

import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.enums.PeriodType;

/**
 * Spring Batch용 PowerUser 데이터 전송 객체
 * Reader에서 읽어온 사용자 활동 데이터를 담는 불변 객체
 */
public record PowerUserData(
    User user,
    PeriodType period,
    Double reviewScoreSum,
    Long likeCount,
    Long commentCount
) {
    /**
     * 기본값 설정 생성자
     */
    public PowerUserData {
        if (reviewScoreSum == null) reviewScoreSum = 0.0;
        if (likeCount == null) likeCount = 0L;
        if (commentCount == null) commentCount = 0L;
    }

    /**
     * 총 점수 계산 (파워 유저 점수 공식)
     * 리뷰 점수 + 좋아요 수 + 댓글 수
     */
    public double totalScore() {
        return reviewScoreSum + (likeCount * 1.0) + (commentCount * 0.5);
    }

    /**
     * 총 활동 건수 계산
     */
    public long getTotalActivityCount() {
        return likeCount + commentCount;
    }

    /**
     * 사용자 정보 요약 (로깅용)
     */
    public String getUserSummary() {
        return String.format("User[%s]: 리뷰점수=%.1f, 좋아요=%d, 댓글=%d",
            user.getNickname(), reviewScoreSum, likeCount, commentCount);
    }
}