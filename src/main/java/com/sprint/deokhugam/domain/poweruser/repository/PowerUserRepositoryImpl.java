package com.sprint.deokhugam.domain.poweruser.repository;

import static com.sprint.deokhugam.domain.comment.entity.QComment.comment;
import static com.sprint.deokhugam.domain.popularreview.entity.QPopularReview.popularReview;
import static com.sprint.deokhugam.domain.poweruser.entity.QPowerUser.powerUser;
import static com.sprint.deokhugam.domain.review.entity.QReview.review;
import static com.sprint.deokhugam.domain.reviewlike.entity.QReviewLike.reviewLike;
import static com.sprint.deokhugam.domain.user.entity.QUser.user;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.service.PowerUserService;
import com.sprint.deokhugam.global.enums.PeriodType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Repository
public class PowerUserRepositoryImpl implements PowerUserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<PowerUserData> findUserActivityData(PeriodType period, Instant startDate, Instant endDate) {
        log.debug("사용자 활동 데이터 조회 시작 - 기간: {}, 시작: {}, 종료: {}", period, startDate, endDate);

        return queryFactory
            .select(Projections.constructor(PowerUserData.class,
                user,
                Expressions.constant(period),
                // 인기 리뷰 점수 합계 조회
                queryFactory.select(popularReview.score.sum().coalesce(0.0))
                    .from(popularReview)
                    .where(popularReview.review.user.eq(user)
                        .and(popularReview.period.eq(period))),
                // 사용자가 한 좋아요 수 (기간별)
                queryFactory.select(reviewLike.id.count().coalesce(0L))
                    .from(reviewLike)
                    .where(reviewLike.user.eq(user)
                        .and(createDateCondition(reviewLike.createdAt, startDate, endDate))),
                // 사용자가 작성한 댓글 수 (기간별)
                queryFactory.select(comment.id.count().coalesce(0L))
                    .from(comment)
                    .where(comment.user.eq(user)
                        .and(createDateCondition(comment.createdAt, startDate, endDate)))
            ))
            .from(user)
            .where(user.isDeleted.isFalse()) // 삭제되지 않은 사용자만
            .fetch()
            .stream()
            .filter(data -> {
                // 활동이 있는 사용자만 필터링
                return data.reviewScoreSum() > 0 ||
                    data.likeCount() > 0 ||
                    data.commentCount() > 0;
            })
            .toList();
    }


    @Override
    @Transactional
    public List<PowerUser> calculateAndCreatePowerUsers(PeriodType period, Instant startDate, Instant endDate) {
        log.info("파워 유저 계산 시작 - 기간: {}", period);

        // 1. 사용자 활동 데이터 조회
        List<PowerUserData> activityData = findUserActivityData(period, startDate, endDate);

        if (activityData.isEmpty()) {
            log.info("활동 데이터가 없어 파워 유저 계산을 건너뜁니다.");
            return List.of();
        }

        // 2. PowerUser 엔티티 생성 및 점수 계산
        List<PowerUser> powerUsers = activityData.stream()
            .map(data -> {
                // 실제 인기 리뷰 점수 사용 (0.0으로 하드코딩하지 말고)
                Double actualReviewScore = data.reviewScoreSum(); // 실제 조회된 값 사용
                Long actualLikeCount = data.likeCount();
                Long actualCommentCount = data.commentCount();

                Double score = PowerUserService.calculateActivityScore(
                    actualReviewScore, actualLikeCount, actualCommentCount);

                log.debug("사용자 {} 점수 계산: 리뷰점수={}, 좋아요={}, 댓글={}, 총점={}",
                    data.user().getNickname(), actualReviewScore, actualLikeCount, actualCommentCount, score);

                return PowerUser.builder()
                    .user(data.user())
                    .period(period)
                    .rank(1L) // 임시 순위, 나중에 재계산됨
                    .score(score)
                    .reviewScoreSum(actualReviewScore) // 실제 인기 리뷰 점수 저장
                    .likeCount(actualLikeCount)
                    .commentCount(actualCommentCount)
                    .build();
            })
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore())) // 내림차순 정렬
            .toList();

        // 3. 순위 재할당
        for (int i = 0; i < powerUsers.size(); i++) {
            Long rank = (long) (i + 1);
            powerUsers.get(i).updateRank(rank);

            log.debug("순위 할당: {} 순위, {} 점수 (리뷰:{}, 좋아요:{}, 댓글:{}), {} 사용자",
                rank, powerUsers.get(i).getScore(), powerUsers.get(i).getReviewScoreSum(),
                powerUsers.get(i).getLikeCount(), powerUsers.get(i).getCommentCount(),
                powerUsers.get(i).getUser().getNickname());
        }

        log.info("파워 유저 계산 완료 - 총 {}명", powerUsers.size());
        return powerUsers;
    }

    @Override
    @Transactional
    public void recalculateRank(PeriodType period) {
        log.info("순위 재계산 시작 - 기간: {}", period);

        List<PowerUser> powerUsers = queryFactory
            .selectFrom(powerUser)
            .where(powerUser.period.eq(period))
            .orderBy(powerUser.score.desc(),
                powerUser.likeCount.desc(),      // 리뷰점수가 0이므로 좋아요 우선
                powerUser.commentCount.desc(),
                powerUser.reviewScoreSum.desc())  // 나중에 활용할 수 있도록 유지
            .fetch();

        for (int i = 0; i < powerUsers.size(); i++) {
            powerUsers.get(i).updateRank((long) (i + 1));
        }

        log.info("순위 재계산 완료 - 총 {}명", powerUsers.size());
    }

    @Override
    public List<PowerUser> findTopPowerUsersNByPeriod(PeriodType period, int limit) {
        return queryFactory
            .selectFrom(powerUser)
            .where(powerUser.period.eq(period))
            .orderBy(powerUser.rank.asc()) // 순위 오름차순 ( 1등, 2등, 3등... )
            .limit(limit)
            .fetch();
    }

    @Override
    public List<PowerUser> findPowerUserHistoryByUserId(UUID userId) {
        return queryFactory
            .selectFrom(powerUser)
            .where(powerUser.user.id.eq(userId))
            .orderBy(powerUser.period.asc(), powerUser.createdAt.desc())
            .fetch();
    }

    @Override
    @Transactional
    public void batchUpsertPowerUsers(List<PowerUser> powerUsers) {
        if (powerUsers.isEmpty()) {
            return;
        }

        int batchSize = 1000;
        for (int i = 0; i < powerUsers.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, powerUsers.size());
            List<PowerUser> batch = powerUsers.subList(i, endIndex);

            for (PowerUser pu : batch) {
                entityManager.merge(pu);
            }
            entityManager.flush();
            entityManager.clear();
        }
    }

    @Override
    public List<PowerUser> findPowerUsersWithCursor(PeriodType period, String direction, int limit, String cursor, String after) {
        OrderSpecifier<?> orderSpecifier = "ASC".equals(direction)
            ? powerUser.rank.asc()
            : powerUser.rank.desc();

        BooleanExpression whereClause = powerUser.period.eq(period);

        if (cursor != null) {
            try {
                Long cursorRank = Long.parseLong(cursor);
                if ("DESC".equals(direction)) {
                    whereClause = whereClause.and(powerUser.rank.lt(cursorRank)); // DESC는 lt
                } else {
                    whereClause = whereClause.and(powerUser.rank.gt(cursorRank)); // ASC는 gt
                }
            } catch (NumberFormatException e) {
                log.warn("잘못된 cursor 형식: {}", cursor);
            }
        }

        // after 파라미터 처리 수정
        if (after != null) {
            try {
                Instant afterTime = Instant.parse(after);
                whereClause = whereClause.and(powerUser.createdAt.gt(afterTime)); // after 시간보다 이후 데이터만
            } catch (Exception e) {
                log.warn("잘못된 after 시간 형식: {}", after);
            }
        }

        return queryFactory
            .selectFrom(powerUser)
            .where(whereClause)
            .orderBy(orderSpecifier, powerUser.createdAt.desc())
            .limit(limit)
            .fetch();
    }

    /**
     * 기간 조건 생성 헬퍼 메서드
     */
    private BooleanExpression createDateCondition(
        com.querydsl.core.types.dsl.DateTimePath<Instant> dateField,
        Instant startDate,
        Instant endDate) {

        if (startDate == null && endDate == null) {
            return null;
        }
        if (startDate == null) {
            return dateField.loe(endDate);
        }
        if (endDate == null) {
            return dateField.goe(startDate);
        }
        return dateField.between(startDate, endDate);
    }

    /**
     * 특정 사용자의 좋아요 수 조회 ( 기간 필터링 적용 )
     */
    private Long getLikeCountForUser(UUID userId, Instant startDate, Instant endDate) {
        Long count = queryFactory
            .select(reviewLike.id.count())
            .from(reviewLike)
            .where(reviewLike.user.id.eq(userId)
                .and(createDateCondition(reviewLike.createdAt, startDate, endDate)))
            .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 특정 사용자의 댓글 수 조회 ( 기간 필터링 적용 )
     */
    private Long getCommentCountForUser(UUID userId, Instant startDate, Instant endDate) {
        Long count = queryFactory
            .select(comment.id.count())
            .from(comment)
            .where(comment.user.id.eq(userId)
                .and(createDateCondition(comment.createdAt, startDate, endDate)))
            .fetchOne();

        return count != null ? count : 0L;
    }
}