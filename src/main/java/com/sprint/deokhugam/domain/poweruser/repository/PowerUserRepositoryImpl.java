package com.sprint.deokhugam.domain.poweruser.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.comment.entity.QComment;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.entity.QPowerUser;
import com.sprint.deokhugam.domain.review.entity.QReview;
import com.sprint.deokhugam.domain.reviewlike.entity.QReviewLike;
import com.sprint.deokhugam.domain.user.entity.QUser;
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
public class PowerUserRepositoryImpl implements PowerUserRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public List<PowerUser> calculateAndCreatePowerUsers(PeriodType period, Instant startDate, Instant endDate) {
        QReview review = QReview.review;
        QUser user = QUser.user;
        QReviewLike reviewLike = QReviewLike.reviewLike;
        QComment comment = QComment.comment;

        var baseQuery = queryFactory
            .from(review)
            .join(review.user, user)
            .leftJoin(reviewLike).on(reviewLike.review.eq(review))
            .leftJoin(comment).on(comment.review.eq(review));

        // 기간 조건 추가 ( ALL_TIME이 아닌 경우만 )
        if (startDate != null && endDate != null) {
            baseQuery = baseQuery
                .where(review.createdAt.between(startDate, endDate)
                    .and(reviewLike.createdAt.between(startDate, endDate).or(reviewLike.createdAt.isNull()))
                    .and(comment.createdAt.between(startDate, endDate).or(comment.createdAt.isNull())));
        }

        // 활동 점수 계산 결과 조회
        var resultTuples = baseQuery
            .select(
                user,
                // ( 리뷰 점수 합 * 0.5 ) + ( 좋아요 수 * 0.2 ) + ( 댓글 수 * 0.3 )
                review.rating.sum().castToNum(Double.class).multiply(0.5)
                    .add(reviewLike.count().multiply(0.2))
                    .add(comment.count().multiply(0.3)),
                review.rating.sum(),
                reviewLike.count(),
                comment.count()
            )
            .groupBy(user.id)
            .having(review.count().gt(0)) // 리뷰가 있는 사용자만
            .orderBy(review.rating.sum().castToNum(Double.class).multiply(0.5)
                .add(reviewLike.count().multiply(0.2))
                .add(comment.count().multiply(0.3)).desc())
            .fetch();

        // PowerUser 객체 생성
        List<PowerUser> powerUsers = new ArrayList<>();
        for (int i = 0; i < resultTuples.size(); i++) {
            var tuple = resultTuples.get(i);

            // Integer를 Double로 안전하게 변환
            Integer ratingSum = tuple.get(review.rating.sum());
            Double reviewScoreSum = ratingSum != null ? ratingSum.doubleValue() : 0.0;

            // 점수 계산
            Long likeCount = tuple.get(reviewLike.count());
            Long commentCount = tuple.get(comment.count());
            Double score = (reviewScoreSum * 0.5) + (likeCount * 0.2) + (commentCount * 0.3);

            PowerUser powerUser = new PowerUser(
                tuple.get(user),                    // User
                period,                             // PeriodType
                (long) (i + 1),                     // Long rank
                score,                              // Double score
                reviewScoreSum,                     // Double reviewScoreSum
                likeCount,                          // Long likeCount
                commentCount                        // Long commentCount
            );
            powerUsers.add(powerUser);
        }

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
            powerUsers.get(i).setRank((long) (i + 1));
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

            if (i % batchSize == 0 && i > 0) {
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

        var query = queryFactory
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
}
