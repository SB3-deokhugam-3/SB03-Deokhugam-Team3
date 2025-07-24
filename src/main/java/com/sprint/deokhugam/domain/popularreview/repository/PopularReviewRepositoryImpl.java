package com.sprint.deokhugam.domain.popularreview.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.book.entity.QBook;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.domain.popularreview.entity.QPopularReview;
import com.sprint.deokhugam.domain.review.entity.QReview;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PopularReviewRepositoryImpl implements PopularReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QPopularReview pr = QPopularReview.popularReview;
    private static final QReview r = QReview.review;

    @Override
    public List<PopularReview> findByPeriodWithCursor(
        PeriodType period,
        Sort.Direction direction,
        String cursor,
        Instant after,
        int limit
    ) {

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(pr.period.eq(period));
        builder.and(r.isDeleted.eq(false));

        if (cursor != null && !cursor.isBlank() && after != null) {
            try {
                Long cursorRank = Long.parseLong(cursor);
                Instant afterTime = Instant.parse(after.toString());

                if (direction == Sort.Direction.ASC) {
                    builder.and(
                        pr.rank.gt(cursorRank)
                            .or(pr.rank.eq(cursorRank).and(pr.createdAt.gt(afterTime)))
                    );
                } else {
                    builder.and(
                        pr.rank.lt(cursorRank)
                            .or(pr.rank.eq(cursorRank).and(pr.createdAt.lt(afterTime)))
                    );
                }
            } catch (NumberFormatException e) {
                log.warn("[PopularReviewRepository] Invalid cursor format: {} ", cursor);
                throw e;
            } catch (DateTimeParseException e) {
                log.warn("[PopularReviewRepository] Invalid after format: {}", after);
                throw e;
            }
        }

        List<OrderSpecifier<?>> orderSpecifiers = (direction == Sort.Direction.ASC)
            ? List.of(pr.rank.asc(), pr.createdAt.asc())
            : List.of(pr.rank.desc(), pr.createdAt.desc());

        return queryFactory
            .selectFrom(pr)
            .join(pr.review, r).fetchJoin()
            .join(r.book, QBook.book).fetchJoin()
            .join(r.user).fetchJoin()
            .where(builder)
            .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
            .limit(limit + 1)
            .fetch();
    }
}
