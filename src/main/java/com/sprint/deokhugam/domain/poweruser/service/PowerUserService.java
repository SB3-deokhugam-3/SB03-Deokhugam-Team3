package com.sprint.deokhugam.domain.poweruser.service;

import com.sprint.deokhugam.domain.poweruser.dto.PowerUserDto;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.repository.PowerUserRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PowerUserService {

    private final PowerUserRepository powerUserRepository;

    public static final double REVIEW_SCORE_WEIGHT = 0.5;
    public static final double LIKE_COUNT_WEIGHT = 0.2;
    public static final double COMMENT_COUNT_WEIGHT = 0.3;

    /**
     * 활동 점수 계산
     * 점수 = ( 리뷰 인기 점수 * 0.5 ) + ( 좋아요 수 * 0.2 ) + ( 댓글 수 * 0.3 )
     * */
    public static Double calculateActivityScore(Double reviewScoreSum, Long likeCount, Long commentCount) {
        if (reviewScoreSum == null) reviewScoreSum = 0.0;
        if (likeCount == null) likeCount = 0L;
        if (commentCount == null) commentCount = 0L;

        return (reviewScoreSum * REVIEW_SCORE_WEIGHT) +
            (likeCount * LIKE_COUNT_WEIGHT) +
            (commentCount * COMMENT_COUNT_WEIGHT);
    }

    /**
     * 기간별 파워 유저 데이터 저장
     * */
    public void savePowerUsers(List<PowerUser> powerUsers) {
        if (!powerUsers.isEmpty()) {
            PeriodType period = powerUsers.get(0).getPeriod();

            // 순위 설정
            for (int i = 0; i < powerUsers.size(); i++) {
                powerUsers.get(i).updateRank((long) (i + 1));
            }

            powerUserRepository.saveAll(powerUsers);
            log.info("파워 유저 데이터 저장 완료: {} 기간, {} 명", period, powerUsers.size());
        }
    }

    /**
     * 기간별 파워 유저 데이터 저장 (기존 데이터 삭제 후)
     */
    @Transactional
    public void replacePowerUsers(List<PowerUser> powerUsers) {
        if (!powerUsers.isEmpty()) {
            PeriodType period = powerUsers.get(0).getPeriod();

            // 기존 데이터 삭제
            powerUserRepository.deleteByPeriod(period);
            log.debug("기존 {} 파워 유저 데이터 삭제 완료", period);

            // 새 데이터 저장
            savePowerUsers(powerUsers);
        }
    }

    /**
     * 기간별 파워 유저 조회 ( 상위 10 명 )
     * */
    public List<PowerUser> getPowerUsersByPeriod(PeriodType period, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return powerUserRepository.findByPeriodOrderByRankAsc(period,pageable);
    }

    /**
     * 커서 기반 파워유저 조회
     */
    public CursorPageResponse<PowerUserDto> getPowerUsersWithCursor(
        PeriodType period, String direction, int size, String cursor, String after) {

        // 입력값 검증
        validateGetPowerUserInput(size, direction, period);

        // 커서 기반 조회 (size + 1로 다음 페이지 존재 여부 확인)
        List<PowerUser> powerUsers = powerUserRepository.findPowerUsersWithCursor(
            period, direction, size + 1, cursor, after);

        // 다음 페이지 존재 여부 확인
        boolean hasNext = powerUsers.size() > size;
        if (hasNext) {
            powerUsers = powerUsers.subList(0, size);
        }

        // PowerUserDto 변환
        List<PowerUserDto> content = powerUsers.stream()
            .map(this::convertToDto)
            .toList();

        // 다음 커서 정보 생성
        String nextCursor = null;
        String nextAfter = null;
        if (hasNext && !powerUsers.isEmpty()) {
            PowerUser lastUser = powerUsers.get(powerUsers.size() - 1);
            nextCursor = lastUser.getRank().toString();
            nextAfter = lastUser.getCreatedAt().toString();
        }

        // 전체 개수 조회
        Long totalElements = powerUserRepository.countByPeriod(period);

        return new CursorPageResponse<>(
            content,
            nextCursor,
            nextAfter,
            size,
            totalElements,
            hasNext
        );
    }

    /**
     * 파워 유저 조회 입력값 검증
     * */
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
            .reviewScoreSum(powerUser.getReviewScoreSum())
            .likeCount(powerUser.getLikeCount())
            .commentCount(powerUser.getCommentCount())
            .build();
    }
}
