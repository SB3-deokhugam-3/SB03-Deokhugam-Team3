package com.sprint.deokhugam.domain.comment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.comment.entity.QComment;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QComment comment = QComment.comment;
    
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
