package com.sprint.deokhugam.domain.poweruser.service;

import com.sprint.deokhugam.domain.popularreview.service.PopularReviewService;
import com.sprint.deokhugam.domain.poweruser.dto.PowerUserDto;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PowerUserService {

    private final PowerUserRepository powerUserRepository;
    private final PopularReviewService popularReviewService;

    public static final double REVIEW_SCORE_WEIGHT = 0.5;  // 현재 0으로 처리하므로 실질적으로 미적용
    public static final double LIKE_COUNT_WEIGHT = 0.2;
    public static final double COMMENT_COUNT_WEIGHT = 0.3;

    /**
     * 활동 점수 계산 - 인기 리뷰 점수를 실제로 반영
     *
     * 공식: 활동 점수 = (리뷰 인기 점수 * 0.5) + (좋아요 수 * 0.2) + (댓글 수 * 0.3)
     */
    public static Double calculateActivityScore(Double reviewScoreSum, Long likeCount, Long commentCount) {
        if (reviewScoreSum == null) reviewScoreSum = 0.0;
        if (likeCount == null) likeCount = 0L;
        if (commentCount == null) commentCount = 0L;

        log.debug("활동 점수 계산 - 리뷰점수: {}, 좋아요: {}, 댓글: {}",
            reviewScoreSum, likeCount, commentCount);

        return (reviewScoreSum * REVIEW_SCORE_WEIGHT) +
            (likeCount * LIKE_COUNT_WEIGHT) +
            (commentCount * COMMENT_COUNT_WEIGHT);
    }

    /**
     * 기간별 파워 유저 데이터 저장
     */
    public void savePowerUsers(List<PowerUser> powerUsers) {
        if (powerUsers == null || powerUsers.isEmpty()) {
            log.warn("저장할 PowerUser 데이터가 없습니다.");
            return;
        }

        // 점수 기준 내림차순 정렬 후 순위 재할당
        List<PowerUser> sortedUsers = sortAndAssignRanks(powerUsers);

        log.info("PowerUser 저장 시작 - {} 건", sortedUsers.size());
        powerUserRepository.saveAll(sortedUsers);
        log.info("PowerUser 저장 완료 - {} 건", sortedUsers.size());
    }

    /**
     * 기간별 파워 유저 데이터 저장 (기존 데이터 삭제 후)
     */
    @Transactional
    public void replacePowerUsers(List<PowerUser> powerUsers) {
        if (powerUsers == null || powerUsers.isEmpty()) {
            log.warn("교체할 PowerUser 데이터가 없습니다.");
            return;
        }

        // 기간 정보 추출 (모든 powerUser가 같은 period를 가진다고 가정)
        PeriodType period = powerUsers.get(0).getPeriod();

        log.info("기존 {} PowerUser 데이터 삭제 시작", period);
        powerUserRepository.deleteByPeriod(period);
        log.info("기존 {} PowerUser 데이터 삭제 완료", period);

        // 점수 기준 내림차순 정렬 후 순위 재할당
        List<PowerUser> sortedUsers = sortAndAssignRanks(powerUsers);

        log.info("{} PowerUser 새 데이터 저장 시작 - {} 건", period, sortedUsers.size());
        powerUserRepository.saveAll(sortedUsers);
        log.info("{} PowerUser 새 데이터 저장 완료 - {} 건", period, sortedUsers.size());
    }

    /**
     * 점수 기준 내림차순 정렬 후 순위 재할당
     */
    private List<PowerUser> sortAndAssignRanks(List<PowerUser> powerUsers) {
        // 1. 점수 기준 내림차순 정렬
        List<PowerUser> sorted = powerUsers.stream()
            .sorted(Comparator.comparing(PowerUser::getScore).reversed()
                .thenComparing(user -> user.getUser().getNickname())) // 동점시만 닉네임으로 구분
            .toList();

        // 2. 순위 재할당 (동점자 처리 포함)
        Double previousScore = null;
        Long actualRank = 1L;

        for (int i = 0; i < sorted.size(); i++) {
            PowerUser user = sorted.get(i);
            Double currentScore = user.getScore();

            // 동점이 아닌 경우 실제 순위 업데이트
            if (previousScore == null || !currentScore.equals(previousScore)) {
                actualRank = (long) (i + 1);
            }

            // 순위 업데이트
            user.updateRank(actualRank);
            previousScore = currentScore;

            log.debug("순위 할당: {} 순위, {} 점수 (리뷰:{}, 좋아요:{}, 댓글:{}), {} 사용자",
                actualRank, currentScore, user.getReviewScoreSum(), user.getLikeCount(),
                user.getCommentCount(), user.getUser().getNickname());
        }

        return sorted;
    }

    /**
     * 기간별 파워 유저 조회 ( 상위 limit 명 )
     */
    public List<PowerUser> getPowerUsersByPeriod(PeriodType period, int limit) {
        validateGetPowerUserInput(limit, "DESC", period);
        return powerUserRepository.findTopPowerUsersNByPeriod(period, limit);
    }

    /**
     * 커서 기반 파워유저 조회
     */
    public CursorPageResponse<PowerUserDto> getPowerUsersWithCursor(
        PeriodType period, String direction, int size, String cursor, String after) {

        validateGetPowerUserInput(size, direction, period);

        List<PowerUser> powerUsers = powerUserRepository.findPowerUsersWithCursor(
            period, direction, size, cursor, after);

        List<PowerUserDto> dtos = powerUsers.stream()
            .map(this::convertToDto)
            .toList();

        long totalElements = powerUserRepository.countByPeriod(period);
        boolean hasNext = powerUsers.size() == size;

        String nextCursor = hasNext && !powerUsers.isEmpty()
            ? String.valueOf(powerUsers.get(powerUsers.size() - 1).getRank())
            : null;

        String nextAfter = hasNext && !powerUsers.isEmpty()
            ? powerUsers.get(powerUsers.size() - 1).getCreatedAt().toString()
            : null;

        return new CursorPageResponse<>(dtos, nextCursor, nextAfter, size, totalElements, hasNext);
    }

    /**
     * 파워 유저 조회 입력값 검증
     */
    private void validateGetPowerUserInput(int limit, String direction, PeriodType period) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit은 1 이상 100 이하여야 합니다.");
        }

        if (!direction.equalsIgnoreCase("ASC") && !direction.equalsIgnoreCase("DESC")) {
            throw new IllegalArgumentException("direction은 ASC 또는 DESC여야 합니다.");
        }

        if (period == null) {
            throw new IllegalArgumentException("period는 필수값입니다.");
        }
    }

    /**
     * PowerUser -> PowerUserDto 변환
     */
    private PowerUserDto convertToDto(PowerUser powerUser) {
        return PowerUserDto.builder()
            .userId(powerUser.getUser().getId())
            .nickname(powerUser.getUser().getNickname())
            .period(powerUser.getPeriod().name())
            .createdAt(powerUser.getCreatedAt())
            .rank(powerUser.getRank())
            .score(powerUser.getScore())
            .reviewScoreSum(powerUser.getReviewScoreSum()) // 실제 저장된 값 사용
            .likeCount(powerUser.getLikeCount())
            .commentCount(powerUser.getCommentCount())
            .build();
    }

    /**
     * 특정 사용자의 기간별 인기 리뷰 점수 합계를 조회
     */
    public Double getUserReviewScoreSum(UUID userId, PeriodType period) {
        try {
            return popularReviewService.getUserPopularityScoreSum(userId, period);
        } catch (Exception e) {
            log.error("인기 리뷰 점수 조회 중 오류 발생 - userId: {}, period: {}", userId, period, e);
            return 0.0;
        }
    }
}