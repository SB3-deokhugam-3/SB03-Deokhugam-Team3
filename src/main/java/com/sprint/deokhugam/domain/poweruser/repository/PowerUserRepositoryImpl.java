package com.sprint.deokhugam.domain.poweruser.repository;

import static com.sprint.deokhugam.domain.comment.entity.QComment.comment;
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
                Expressions.constant(period), // PeriodType을 상수로 전달
                Expressions.numberTemplate(Double.class, "0.0"), // 리뷰 점수는 임시로 0.0으로 설정
                review.id.count().coalesce(0L), // 실제로는 리뷰 수 ( 임시 )
                comment.id.count().coalesce(0L) // 댓글 수
            ))
            .from(user)
            .leftJoin(review).on(review.user.eq(user)
                .and(createDateCondition(review.createdAt, startDate, endDate)))
            .leftJoin(comment).on(comment.user.eq(user)
                .and(createDateCondition(comment.createdAt, startDate, endDate)))
            .groupBy(user.id)
            .having(
                // 리뷰 수 + 댓글 수 > 0인 활성 사용자만 조회
                review.id.count().coalesce(0L)
                    .add(comment.id.count().coalesce(0L)).gt(0))
            .fetch();
    }

    @Override
    @Transactional
    public List<PowerUser> calculateAndCreatePowerUsers(PeriodType period, Instant startDate, Instant endDate) {
        log.info("파워 유저 계산 시작 - 기간: {} (리뷰 인기 점수는 임시로 0 처리)", period);

        // 1. 사용자 활동 데이터 조회
        List<PowerUserData> activityData = findUserActivityData(period, startDate, endDate);

        if (activityData.isEmpty()) {
            log.info("활동 데이터가 없어 파워 유저 계산을 건너뜁니다.");
            return List.of();
        }

        // 2. PowerUser 엔티티 생성 및 점수 계산
        List<PowerUser> powerUsers = activityData.stream()
            .map(data -> {
                // 실제 좋아요 수와 댓글 수 조회
                Long actualLikeCount = getLikeCountForUser(data.user().getId(), startDate, endDate);
                Long actualCommentCount = getCommentCountForUser(data.user().getId(), startDate, endDate);

                // 리뷰 인기 점수는 임시로 0.0으로 설정
                Double tempReviewScore = 0.0;

                Double score = PowerUserService.calculateActivityScore(
                    tempReviewScore, actualLikeCount, actualCommentCount);

                log.debug("사용자 {} 점수 계산: 리뷰점수={} (임시0), 좋아요={}, 댓글={}, 총점={}",
                    data.user().getNickname(), tempReviewScore, actualLikeCount, actualCommentCount, score);

                return PowerUser.builder()
                    .user(data.user())
                    .period(period)
                    .rank(1L) // 임시 순위, 나중에 재계산됨
                    .score(score)
                    .reviewScoreSum(tempReviewScore) // 임시로 0.0 저장
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

            log.debug("순위 할당: {} 순위, {} 점수 (좋아요:{}, 댓글:{}), {} 사용자",
                rank, powerUsers.get(i).getScore(), powerUsers.get(i).getLikeCount(),
                powerUsers.get(i).getCommentCount(), powerUsers.get(i).getUser().getNickname());
        }

        log.info("파워 유저 계산 완료 - 총 {}명 (리뷰 점수는 임시로 0 처리됨)", powerUsers.size());
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