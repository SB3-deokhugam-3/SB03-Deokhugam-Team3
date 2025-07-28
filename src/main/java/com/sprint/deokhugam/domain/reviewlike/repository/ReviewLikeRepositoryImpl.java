package com.sprint.deokhugam.domain.reviewlike.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.reviewlike.entity.QReviewLike;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewLikeRepositoryImpl implements ReviewLikeRepositoryCustom {

    private static final QReviewLike reviewLike = QReviewLike.reviewLike;
    private final JPAQueryFactory queryFactory;

    // 특정 기간 동안 리뷰에 눌린 좋아요 개수
    @Override
    public Map<UUID, Long> countByReviewIdBetween(Instant start, Instant end) {

        return queryFactory
            .select(reviewLike.review.id, reviewLike.count())
            .from(reviewLike)
            .where(
                reviewLike.createdAt.gt(start),
                reviewLike.createdAt.lt(end)
            )
            .groupBy(reviewLike.review.id)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(reviewLike.review.id),
                tuple -> tuple.get(reviewLike.count())
            ));
    }
}
