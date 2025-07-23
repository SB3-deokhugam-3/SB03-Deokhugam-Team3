package com.sprint.deokhugam.domain.popularreview.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.book.entity.QBook;
import com.sprint.deokhugam.domain.book.storage.s3.S3Storage;
import com.sprint.deokhugam.domain.popularreview.PeriodType;
import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.domain.popularreview.entity.QPopularReview;
import com.sprint.deokhugam.domain.popularreview.mapper.PopularReviewMapper;
import com.sprint.deokhugam.domain.review.entity.QReview;
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
    private final S3Storage s3Storage;
    private final PopularReviewMapper popularReviewMapper;
    private static final QPopularReview pr = QPopularReview.popularReview;
    private static final QReview r = QReview.review;


//
//    @Override
//    public List<PopularReviewStats> calculatePopularReviews(Instant from, Instant to) {
//        QReview review = QReview.review;
//        QReviewLike reviewLike = QReviewLike.reviewLike;
//        QComment comment = QComment.comment;
//
//        return queryFactory
//            .select(Projections.constructor(PopularReviewStats.class,
//                review.id,
//                reviewLike.id.countDistinct().coalesce(0L),
//                comment.id.countDistinct().coalesce(0L)
//            ))
//            .from(review)
//            .leftJoin(reviewLike).on(
//                reviewLike.review.id.eq(review.id),
//                reviewLike.createdAt.between(from, to)
//            )
//            .leftJoin(comment).on(
//                comment.review.id.eq(review.id),
//                comment.createdAt.between(from, to)
//            )
//            .groupBy(review.id)
//            .fetch();
//    }

    @Override
    public List<PopularReviewDto> findByPeriodWithCursor(
        PeriodType period,
        Sort.Direction direction,
        String cursor,
        Instant after,
        int limit
    ) {
        QPopularReview pr = QPopularReview.popularReview;

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

        List<PopularReview> entities = queryFactory
            .selectFrom(pr)
            .join(pr.review, r).fetchJoin()
            .join(r.book, QBook.book).fetchJoin()
            .where(builder)
            .orderBy(orderSpecifiers.toArray(new com.querydsl.core.types.OrderSpecifier[0]))
            .limit(limit + 1)
            .fetch();

        List<PopularReviewDto> dtos = entities.stream()
            .map(popularReview -> popularReviewMapper.toDto(popularReview, s3Storage))
            .toList();

        return dtos;
    }
}
