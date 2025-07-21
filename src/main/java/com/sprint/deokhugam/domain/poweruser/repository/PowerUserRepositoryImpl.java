package com.sprint.deokhugam.domain.poweruser.repository;

import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.comment.entity.QComment;
import com.sprint.deokhugam.domain.poweruser.entity.PowerUser;
import com.sprint.deokhugam.domain.poweruser.entity.QPowerUser;
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
import java.util.Optional;
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

        // 1. 기본 리뷰 점수 집계
        var baseQuery = queryFactory
            .from(review)
            .join(review.user, user);

        if (startDate != null && endDate != null) {
            baseQuery = baseQuery.where(review.createdAt.between(startDate, endDate));
        }

        var reviewData = baseQuery
            .select(
                user.id,
                user,
                review.rating.sum()
            )
            .groupBy(user.id)
            .having(review.count().gt(0))
            .fetch();

        List<PowerUser> powerUsers = new ArrayList<>();

        for (var tuple : reviewData) {
            UUID userId = tuple.get(user.id);
            User userEntity = tuple.get(user);
            Integer ratingSum = tuple.get(review.rating.sum());
            Double reviewScoreSum = ratingSum != null ? ratingSum.doubleValue() : 0.0;

            // 2. 해당 사용자의 좋아요 수 조회
            var likeQuery = queryFactory
                .select(reviewLike.count())
                .from(reviewLike)
                .join(reviewLike.review, review)
                .where(review.user.id.eq(userId));

            if (startDate != null && endDate != null) {
                likeQuery = likeQuery.where(reviewLike.createdAt.between(startDate, endDate));
            }

            Long likeCount = likeQuery.fetchOne();
            if (likeCount == null) likeCount = 0L;

            // 3. 해당 사용자의 댓글 수 조회
            var commentQuery = queryFactory
                .select(comment.count())
                .from(comment)
                .join(comment.review, review)
                .where(review.user.id.eq(userId));

            if (startDate != null && endDate != null) {
                commentQuery = commentQuery.where(comment.createdAt.between(startDate, endDate));
            }

            Long commentCount = commentQuery.fetchOne();
            if (commentCount == null) commentCount = 0L;

            // 4. 최종 점수 계산
            Double score = (reviewScoreSum * 0.5) + (likeCount * 0.2) + (commentCount * 0.3);

            PowerUser powerUser = new PowerUser(
                userEntity,
                period,
                1L, // 임시 순위, 나중에 재정렬
                score,
                reviewScoreSum,
                likeCount,
                commentCount
            );
            powerUsers.add(powerUser);
        }

        // 점수 기준으로 정렬하고 순위 재할당
        powerUsers.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        for (int i = 0; i < powerUsers.size(); i++) {
            powerUsers.get(i).updateRank((long) (i + 1));
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

        var query = queryFactory
            .selectFrom(powerUser)
            .join(powerUser.user, user).fetchJoin()
            .where(powerUser.period.eq(period));

        // 커서 기반 필터링 ( 순위 기준)
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

        // after 시간 기준 필터링 ( 추가 정렬 조건 )
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

    /**
     * 기간별 좋아요 수 계산 표현식
     */
    private NumberExpression<Long> getLikeCountExpression(
        QReviewLike reviewLike, Instant startDate, Instant endDate) {

        if (startDate != null && endDate != null) {
            // 기간 내 좋아요만 카운트
            return reviewLike.id.countDistinct();
        } else {
            // ALL_TIME의 경우 전체 좋아요 수
            return reviewLike.id.countDistinct();
        }
    }

    /**
     * 기간별 댓글 수 계산 표현식
     */
    private NumberExpression<Long> getCommentCountExpression(
        QComment comment, Instant startDate, Instant endDate) {

        if (startDate != null && endDate != null) {
            // 기간 내 댓글만 카운트
            return comment.id.countDistinct();
        } else {
            // ALL_TIME의 경우 전체 댓글 수
            return comment.id.countDistinct();
        }
    }
}
