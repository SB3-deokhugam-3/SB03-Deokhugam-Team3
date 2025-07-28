package com.sprint.deokhugam.domain.comment.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.comment.entity.Comment;
import com.sprint.deokhugam.domain.comment.entity.QComment;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomCommentRepositoryImpl implements CustomCommentRepository {

    private static final QComment comment = QComment.comment;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> fetchComments(
        UUID reviewId,
        Instant cursor,
        Instant after,
        Sort.Direction direction,
        int fetchSize
    ) {
        QComment comment = QComment.comment;

        BooleanBuilder where = new BooleanBuilder()
            .and(comment.review.id.eq(reviewId))
            .and(comment.isDeleted.isFalse());

        if (cursor != null) {
            if (direction.isAscending()) {
                where.and(comment.createdAt.gt(cursor));
            } else {
                where.and(comment.createdAt.lt(cursor));
            }
        }

        if (after != null) {
            if (direction.isAscending()) {
                where.and(comment.createdAt.gt(after));
            } else {
                where.and(comment.createdAt.lt(after));
            }
        }

        OrderSpecifier<?> orderSpecifier = direction.isAscending()
            ? comment.createdAt.asc()
            : comment.createdAt.desc();

        return queryFactory
            .selectFrom(comment)
            .where(where)
            .orderBy(orderSpecifier)
            .limit(fetchSize)
            .fetch();
    }

    @Override
    public Map<UUID, Long> countByReviewIdBetween(Instant start, Instant end) {

        return queryFactory
            .select(comment.review.id, comment.count())
            .from(comment)
            .where(
                comment.createdAt.gt(start),
                comment.createdAt.lt(end),
                comment.isDeleted.isFalse()
            )
            .groupBy(comment.review.id)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(comment.review.id),
                tuple -> tuple.get(comment.count())
            ));
    }
}
