package com.sprint.deokhugam.domain.comment.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.comment.entity.Comment;
import com.sprint.deokhugam.domain.comment.entity.QComment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomCommentRepositoryImpl implements CustomCommentRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> fetchComments(
        UUID reviewId,
        Instant cursor,
        UUID after,
        Sort.Direction direction,
        int fetchSize
    ) {
        QComment comment = QComment.comment;

        BooleanBuilder where = new BooleanBuilder()
            .and(comment.review.id.eq(reviewId))
            .and(comment.isDeleted.isFalse());

        // cursor 기반 커서 페이지네이션
        if (cursor != null) {
            if (direction.isAscending()) {
                where.and(
                    comment.createdAt.gt(cursor)
                        .or(comment.createdAt.eq(cursor).and(comment.id.gt(after)))
                );
            } else {
                where.and(
                    comment.createdAt.lt(cursor)
                        .or(comment.createdAt.eq(cursor).and(comment.id.lt(after)))
                );
            }
        }

        OrderSpecifier<?>[] orderSpecifiers = direction.isAscending()
            ? new OrderSpecifier[]{comment.createdAt.asc(), comment.id.asc()}
            : new OrderSpecifier[]{comment.createdAt.desc(), comment.id.desc()};

        return queryFactory
            .selectFrom(comment)
            .where(where)
            .orderBy(orderSpecifiers)
            .limit(fetchSize)
            .fetch();
    }
}
