package com.sprint.deokhugam.domain.poweruser.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.comment.entity.QComment;
import com.sprint.deokhugam.domain.poweruser.dto.batch.PowerUserData;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.entity.QPowerUser;
import com.sprint.deokhugam.domain.poweruser.service.PowerUserService;
import com.sprint.deokhugam.domain.review.entity.QReview;
import com.sprint.deokhugam.domain.reviewlike.entity.QReviewLike;
import com.sprint.deokhugam.domain.user.entity.QUser;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.enums.PeriodType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.ArrayList;
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
        log.info("사용자 활동 데이터 조회 시작 - 기간: {}, 범위: {} ~ {}", period, startDate, endDate);

        QReview review = QReview.review;
        QUser user = QUser.user;
        QReviewLike reviewLike = QReviewLike.reviewLike;
        QComment comment = QComment.comment;

        // 기간 조건 생성
        BooleanExpression dateCondition = createDateCondition(review.createdAt, startDate, endDate);

        // 1. 리뷰 기반 기본 데이터 조회
        List<Tuple> reviewData = queryFactory
            .select(
                user.id,
                user,
                review.rating.sum()
            )
            .from(review)
            .join(review.user, user)
            .where(dateCondition)
            .groupBy(user.id)
            .having(review.count().gt(0))
            .fetch();

        List<PowerUserData> powerUserDataList = new ArrayList<>();

        for (Tuple tuple : reviewData) {
            UUID userId = tuple.get(user.id);
            User userEntity = tuple.get(user);
            Integer ratingSum = tuple.get(review.rating.sum());
            Double reviewScoreSum = ratingSum != null ? ratingSum.doubleValue() : 0.0;

            // 2. 해당 사용자의 좋아요 수 조회 (기간 필터링 적용)
            Long likeCount = getLikeCountForUser(userId, startDate, endDate);

            // 3. 해당 사용자의 댓글 수 조회 (기간 필터링 적용)
            Long commentCount = getCommentCountForUser(userId, startDate, endDate);

            PowerUserData powerUserData = new PowerUserData(
                userEntity,
                period,
                reviewScoreSum,
                likeCount,
                commentCount
            );

            powerUserDataList.add(powerUserData);
            log.debug("사용자 활동 데이터 생성: {}", powerUserData.getUserSummary());
        }

        log.info("사용자 활동 데이터 조회 완료 - {} 기간, {} 명", period, powerUserDataList.size());
        return powerUserDataList;
    }

    @Override
    @Transactional
    public List<PowerUser> calculateAndCreatePowerUsers(PeriodType period, Instant startDate, Instant endDate) {
        log.info("파워유저 계산 시작 - 기간: {}, 범위: {} ~ {}", period, startDate, endDate);

        QReview review = QReview.review;
        QUser user = QUser.user;

        // 기간 조건 생성
        BooleanExpression dateCondition = createDateCondition(review.createdAt, startDate, endDate);

        // 1. 리뷰 기반 기본 데이터 조회
        List<Tuple> reviewData = queryFactory
            .select(
                user.id,
                user,
                review.rating.sum()
            )
            .from(review)
            .join(review.user, user)
            .where(dateCondition)
            .groupBy(user.id)
            .having(review.count().gt(0))
            .fetch();

        log.info("리뷰 작성 사용자 수: {} 명", reviewData.size());

        List<PowerUser> powerUsers = new ArrayList<>();

        for (Tuple tuple : reviewData) {
            UUID userId = tuple.get(user.id);
            User userEntity = tuple.get(user);
            Integer ratingSum = tuple.get(review.rating.sum());
            Double reviewScoreSum = ratingSum != null ? ratingSum.doubleValue() : 0.0;

            // 2. 해당 사용자의 좋아요 수 조회 (기간 필터링 적용)
            Long likeCount = getLikeCountForUser(userId, startDate, endDate);

            // 3. 해당 사용자의 댓글 수 조회 (기간 필터링 적용)
            Long commentCount = getCommentCountForUser(userId, startDate, endDate);

            // 4. 최종 점수 계산
            Double score = PowerUserService.calculateActivityScore(reviewScoreSum, likeCount, commentCount);

            PowerUser powerUser = new PowerUser(
                userEntity,
                period,
                1L, // 임시 순위 - 아래에서 점수순으로 재할당
                score,
                reviewScoreSum,
                likeCount,
                commentCount
            );
            powerUsers.add(powerUser);

            log.debug("파워유저 생성: {} (점수: {})", userEntity.getNickname(), score);
        }

        // 점수 기준으로 정렬하고 순위 재할당
        powerUsers.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        for (int i = 0; i < powerUsers.size(); i++) {
            powerUsers.get(i).updateRank((long) (i + 1));
        }

        log.info("파워유저 계산 완료 - {} 기간, {} 명", period, powerUsers.size());
        return powerUsers;
    }

    @Override
    @Transactional
    public void recalculateRank(PeriodType period) {
        QPowerUser powerUser = QPowerUser.powerUser;

        List<PowerUser> powerUsers = queryFactory
            .selectFrom(powerUser)
            .where(powerUser.period.eq(period))
            .orderBy(powerUser.score.desc())
            .fetch();

        for (int i = 0; i < powerUsers.size(); i++) {
            powerUsers.get(i).updateRank((long) (i + 1));
        }

        // 배치 업데이트
        batchUpsertPowerUsers(powerUsers);
    }

    @Override
    public List<PowerUser> findTopPowerUsersNByPeriod(PeriodType period, int limit) {
        QPowerUser powerUser = QPowerUser.powerUser;
        QUser user = QUser.user;

        return queryFactory
            .selectFrom(powerUser)
            .join(powerUser.user, user).fetchJoin()
            .where(powerUser.period.eq(period))
            .orderBy(powerUser.rank.asc())
            .limit(limit)
            .fetch();
    }

    @Override
    public List<PowerUser> findPowerUserHistoryByUserId(UUID userId) {
        QPowerUser powerUser = QPowerUser.powerUser;
        QUser user = QUser.user;

        return queryFactory
            .selectFrom(powerUser)
            .join(powerUser.user, user).fetchJoin()
            .where(powerUser.user.id.eq(userId))
            .orderBy(powerUser.period.asc(), powerUser.createdAt.desc())
            .fetch();
    }

    @Override
    @Transactional
    public void batchUpsertPowerUsers(List<PowerUser> powerUsers) {
        int batchSize = 100;

        for (int i = 0; i < powerUsers.size(); i++) {
            entityManager.merge(powerUsers.get(i));

            if ((i + 1) % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public List<PowerUser> findPowerUsersWithCursor(PeriodType period, String direction, int limit, String cursor, String after) {
        QPowerUser powerUser = QPowerUser.powerUser;
        QUser user = QUser.user;

        JPAQuery<PowerUser> query = queryFactory
            .selectFrom(powerUser)
            .join(powerUser.user, user).fetchJoin()
            .where(powerUser.period.eq(period));

        // 커서 기반 필터링 (순위 기준)
        if (cursor != null && !cursor.isEmpty()) {
            try {
                Long cursorRank = Long.parseLong(cursor);
                if ("DESC".equalsIgnoreCase(direction)) {
                    query = query.where(powerUser.rank.lt(cursorRank));
                } else {
                    query = query.where(powerUser.rank.gt(cursorRank));
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid cursor format: {}", cursor);
            }
        }

        // after 시간 기준 필터링 (추가 정렬 조건)
        if (after != null && !after.isEmpty()) {
            try {
                Instant afterTime = Instant.parse(after);
                if ("DESC".equalsIgnoreCase(direction)) {
                    query = query.where(powerUser.createdAt.lt(afterTime));
                } else {
                    query = query.where(powerUser.createdAt.gt(afterTime));
                }
            } catch (Exception e) {
                log.warn("Invalid after format: {}", after);
            }
        }

        // 정렬 방향 적용
        if ("DESC".equalsIgnoreCase(direction)) {
            query = query.orderBy(powerUser.rank.desc());
        } else {
            query = query.orderBy(powerUser.rank.asc());
        }

        return query.limit(limit).fetch();
    }

    // 🛠️ 헬퍼 메서드들

    /**
     * 기간 조건 생성 헬퍼 메서드
     */
    private BooleanExpression createDateCondition(
        com.querydsl.core.types.dsl.DateTimePath<Instant> dateField,
        Instant startDate,
        Instant endDate) {

        if (startDate != null && endDate != null) {
            log.debug("기간 필터링 적용: {} ~ {}", startDate, endDate);
            return dateField.between(startDate, endDate);
        }
        log.debug("전체 기간 조회 (ALL_TIME)");
        return null;
    }

    /**
     * 특정 사용자의 좋아요 수 조회 ( 기간 필터링 적용 )
     */
    private Long getLikeCountForUser(UUID userId, Instant startDate, Instant endDate) {
        QReview review = QReview.review;
        QReviewLike reviewLike = QReviewLike.reviewLike;

        BooleanExpression dateCondition = createDateCondition(reviewLike.createdAt, startDate, endDate);

        Long likeCount = queryFactory
            .select(reviewLike.count())
            .from(reviewLike)
            .join(reviewLike.review, review)
            .where(review.user.id.eq(userId)
                .and(dateCondition))
            .fetchOne();

        return likeCount != null ? likeCount : 0L;
    }

    /**
     * 특정 사용자의 댓글 수 조회 ( 기간 필터링 적용 )
     */
    private Long getCommentCountForUser(UUID userId, Instant startDate, Instant endDate) {
        QReview review = QReview.review;
        QComment comment = QComment.comment;

        BooleanExpression dateCondition = createDateCondition(comment.createdAt, startDate, endDate);

        Long commentCount = queryFactory
            .select(comment.count())
            .from(comment)
            .join(comment.review, review)
            .where(review.user.id.eq(userId)
                .and(dateCondition))
            .fetchOne();

        return commentCount != null ? commentCount : 0L;
    }
}