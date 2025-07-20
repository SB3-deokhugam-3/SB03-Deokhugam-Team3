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

        // 기간별 사영자 활동 점수 계산
        List<PowerUser> powerUsers = queryFactory
            .select(Projections.constructor(PowerUser.class,
                user,
                // score = (리뷰 점수 합 * 0.4) + (좋아요 수 * 0.3) + (댓글 수 * 0.3)
                review.rating.sum().multiply(0.4)
                    .add(reviewLike.count().multiply(0.3))
                    .add(comment.count().multiply(0.3)),
                review.rating.sum(),
                reviewLike.count(),
                comment.count()
            ))
            .from(review)
            .join(review.user, user)
            .leftJoin(reviewLike).on(reviewLike.review.eq(review)
                .and(reviewLike.createdAt.between(startDate,endDate)))
            .leftJoin(comment).on(comment.review.eq(review)
                .and(comment.createdAt.between(startDate,endDate)))
            .where(review.createdAt.between(startDate, endDate))
            .groupBy(user.id)
            .having(review.count().gt(0)) // 리뷰가 있는 사용자만
            .orderBy(review.rating.sum().multiply(0.4)
                .add(reviewLike.count().multiply(0.3))
                .add(comment.count().multiply(0.3)).desc())
            .fetch();

        // 순위 부여
        for (int i = 0; i < powerUsers.size(); i++) {
            powerUsers.get(i).setRank((long) (i + 1));
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
}
