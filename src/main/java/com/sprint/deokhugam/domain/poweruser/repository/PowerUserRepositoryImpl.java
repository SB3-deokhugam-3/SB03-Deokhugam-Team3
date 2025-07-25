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
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.comment.entity.QComment;
import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.service.PowerUserService;
import com.sprint.deokhugam.domain.review.entity.QReview;
import com.sprint.deokhugam.domain.user.entity.QUser;
import com.sprint.deokhugam.global.enums.PeriodType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.Collections;
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
        log.info("=== 사용자 활동 데이터 조회 시작 ===");
        log.info("기간: {}", period);
        log.info("시작: {}", startDate);
        log.info("종료: {}", endDate);

        // 먼저 기본 사용자 수 확인
        Long totalUsers = queryFactory
            .select(user.count())
            .from(user)
            .fetchOne();
        log.info("전체 사용자 수: {}", totalUsers);

        // 기간 내 리뷰 수 확인
        Long reviewCount = queryFactory
            .select(review.count())
            .from(review)
            .where(createDateCondition(review.createdAt, startDate, endDate))
            .fetchOne();
        log.info("기간 내 리뷰 수: {}", reviewCount);

        // 기간 내 댓글 수 확인
        Long commentCount = queryFactory
            .select(comment.count())
            .from(comment)
            .where(createDateCondition(comment.createdAt, startDate, endDate))
            .fetchOne();
        log.info("기간 내 댓글 수: {}", commentCount);

        // 기간 내 좋아요 수 확인
        Long likeCount = queryFactory
            .select(reviewLike.count())
            .from(reviewLike)
            .where(createDateCondition(reviewLike.createdAt, startDate, endDate))
            .fetchOne();
        log.info("기간 내 좋아요 수: {}", likeCount);

        // 원래 쿼리 실행
        List<PowerUserData> result = queryFactory
            .select(Projections.constructor(PowerUserData.class,
                user,
                Expressions.constant(period),
                // PopularReview에서 해당 사용자의 점수 합계 조회 (서브쿼리)
                JPAExpressions.select(popularReview.score.sum().coalesce(0.0))
                    .from(popularReview)
                    .join(popularReview.review, QReview.review)
                    .where(QReview.review.user.eq(user)
                        .and(popularReview.period.eq(period))),
                // 좋아요 수 (ReviewLike 테이블에서 조회)
                JPAExpressions.select(reviewLike.id.count().coalesce(0L))
                    .from(reviewLike)
                    .join(reviewLike.review, QReview.review)
                    .where(QReview.review.user.eq(user)
                        .and(createDateCondition(reviewLike.createdAt, startDate, endDate))),
                // 댓글 수
                comment.id.count().coalesce(0L)
            ))
            .from(user)
            .leftJoin(review).on(review.user.eq(user)
                .and(createDateCondition(review.createdAt, startDate, endDate)))
            .leftJoin(comment).on(comment.user.eq(user)
                .and(createDateCondition(comment.createdAt, startDate, endDate)))
            .groupBy(user.id)
            .having(
                // 활동이 있는 사용자만 (리뷰, 댓글, 좋아요 중 하나라도 있으면)
                review.id.count().coalesce(0L)
                    .add(comment.id.count().coalesce(0L)).gt(0)
                    // 또는 PopularReview 점수가 있으면
                    .or(JPAExpressions.select(popularReview.score.sum().coalesce(0.0))
                        .from(popularReview)
                        .join(popularReview.review, QReview.review)
                        .where(QReview.review.user.eq(user)
                            .and(popularReview.period.eq(period))).gt(0.0))
            )
            .fetch();

        log.info("=== 쿼리 결과 ===");
        log.info("조회된 PowerUserData 수: {}", result.size());
        result.stream()
            .limit(5)
            .forEach(data -> log.info("샘플 데이터: 사용자={}, 기간={}, 점수={}, 좋아요={}, 댓글={}",
                data.user().getNickname(), data.period(), data.reviewScoreSum(), data.likeCount(), data.commentCount()));

        return result;
    }

    /**
     * 테스트용 간단한 쿼리 - 복잡한 서브쿼리 없이 기본 활동만 조회
     */
    public List<PowerUserData> findUserActivityDataSimple(PeriodType period, Instant startDate, Instant endDate) {
        log.info("=== 간단한 사용자 활동 데이터 조회 시작 ===");
        log.info("기간: {}, 시작: {}, 종료: {}", period, startDate, endDate);

        // 전체 사용자 수 확인
        Long totalUsers = queryFactory
            .select(user.count())
            .from(user)
            .fetchOne();
        log.info("전체 사용자 수: {}", totalUsers);

        // 랜덤 점수로 사용자 데이터 생성 (테스트용)
        List<PowerUserData> result = queryFactory
            .select(Projections.constructor(PowerUserData.class,
                user,
                Expressions.constant(period),
                Expressions.numberTemplate(Double.class, "RANDOM() * 100"), // 랜덤 점수 0-100
                Expressions.numberTemplate(Long.class, "CAST(RANDOM() * 50 AS BIGINT)"), // 랜덤 좋아요 0-50
                Expressions.numberTemplate(Long.class, "CAST(RANDOM() * 30 AS BIGINT)")  // 랜덤 댓글 0-30
            ))
            .from(user)
            .limit(10) // 10명만
            .fetch();

        log.info("랜덤 테스트 결과: {} 건", result.size());

        // 결과 샘플 출력
        result.forEach(data ->
            log.info("샘플: {} - 점수:{}, 좋아요:{}, 댓글:{}",
                data.user().getNickname(),
                data.reviewScoreSum(),
                data.likeCount(),
                data.commentCount()
            )
        );

        return result;
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
                    .reviewScoreSum(tempReviewScore)
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