package com.sprint.deokhugam.domain.review.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.review.dto.request.ReviewGetRequest;
import com.sprint.deokhugam.domain.review.entity.QReview;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.global.exception.InvalidTypeException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CustomReviewRepositoryImpl implements CustomReviewRepository {

    private final JPAQueryFactory queryFactory;

    private static final String ORDER_BY_CREATED_AT = "createdAt";
    private static final String ORDER_BY_RATING = "rating";
    private static final String ORDER_DIRECTION_DESC = "DESC";

    public List<Review> findAll(ReviewGetRequest params) {
        QReview review = QReview.review;
        String direction = params.direction();
        String orderBy = params.orderBy();

        BooleanBuilder whereCondition = new BooleanBuilder();
        BooleanBuilder cursorCondition = new BooleanBuilder();

        if (params.userId() != null || params.bookId() != null
            || params.keyword() != null) {
            whereCondition.and(filterByIdAndKeyword(review, params));
        }
        //Q. after만 오는 경우는 의미없음
        boolean isCursorExisted = params.cursor() != null;
        if (isCursorExisted) {
            cursorCondition =
                orderBy.equals(ORDER_BY_CREATED_AT) ? filterByCreatedAt(review, params)
                    : filterByRating(review, params);
        }

        return queryFactory
            .selectFrom(review)
            .where(whereCondition, cursorCondition)
            .orderBy(getOrderSpecifiers(review, orderBy, direction))
            .limit(params.limit())
            .fetch();
    }

    public Long countAllByFilterCondition(ReviewGetRequest params) {
        QReview review = QReview.review;

        BooleanBuilder whereCondition = new BooleanBuilder();

        if (params.userId() != null || params.bookId() != null || params.keyword() != null) {
            whereCondition.and(filterByIdAndKeyword(review, params));
        }

        return queryFactory
            .select(review.count())
            .from(review)
            .where(whereCondition)
            .fetchOne();

    }

    private BooleanBuilder filterByIdAndKeyword(QReview review, ReviewGetRequest params) {
        BooleanBuilder whereCondition = new BooleanBuilder();
        String keyword = params.keyword();
        UUID userId = params.userId();
        UUID bookId = params.bookId();

        if (userId != null) {
            whereCondition.and(review.user.id.eq(userId));
        }
        if (bookId != null) {
            whereCondition.and(review.book.id.eq(bookId));
        }
        /* keyword 조건 */
        if (keyword != null) {
            whereCondition.and(
                review.user.nickname.containsIgnoreCase(keyword)
                    .or(review.book.title.containsIgnoreCase(keyword)
                        .or(review.book.description.containsIgnoreCase(keyword)))
            );

        }

        return whereCondition;
    }

    private BooleanBuilder filterByCreatedAt(QReview review, ReviewGetRequest params) {
        try {
            BooleanBuilder whereCondition = new BooleanBuilder();
            String direction = params.direction();
            Instant cursor = Instant.parse(params.cursor().toString());
            if (direction.equals(ORDER_DIRECTION_DESC)) {
                whereCondition
                    .or(review.createdAt.lt(cursor));
            } else {
                whereCondition
                    .or(review.createdAt.gt(cursor));
            }
            return whereCondition;
        } catch (DateTimeParseException e) {
            throw new InvalidTypeException("review",
                Map.of("requestedCursor", params.cursor().toString()));
        }

    }

    private BooleanBuilder filterByRating(QReview review, ReviewGetRequest params) {
        try {
            BooleanBuilder whereCondition = new BooleanBuilder();
            String direction = params.direction();
            Integer cursor = Integer.valueOf(params.cursor().toString());
            Instant after = params.after();

            if (direction.equals(ORDER_DIRECTION_DESC)) {
                whereCondition
                    .or(review.rating.lt(cursor))
                    .or(review.rating.eq(cursor).and(review.createdAt.lt(after)));
            } else {
                whereCondition
                    .or(review.rating.gt(cursor))
                    .or(review.rating.eq(cursor).and(review.createdAt.gt(after)));
            }
            return whereCondition;
        } catch (NumberFormatException e) {
            throw new InvalidTypeException("review",
                Map.of("requestedCursor", params.cursor().toString()));
        }

    }

    private OrderSpecifier<?>[] getOrderSpecifiers(QReview review, String orderBy,
        String direction) {
        OrderSpecifier<?> primary;
        OrderSpecifier<?> secondary;
        // 다른 order 조건을 거치고도 정렬이 고정되지 않는다면, id값으로 최종정렬
        OrderSpecifier<?> defaultOrder =
            direction.equals(ORDER_DIRECTION_DESC) ? review.id.desc() : review.id.asc();
        switch (orderBy) {
            case ORDER_BY_CREATED_AT:
                primary =
                    direction.equals(ORDER_DIRECTION_DESC) ? review.createdAt.desc()
                        : review.createdAt.asc();
                return new OrderSpecifier[]{primary, defaultOrder};
            case ORDER_BY_RATING:
                primary = direction.equals(ORDER_DIRECTION_DESC) ? review.rating.desc()
                    : review.rating.asc();
                secondary =
                    direction.equals(ORDER_DIRECTION_DESC) ? review.createdAt.desc()
                        : review.createdAt.asc();
                return new OrderSpecifier[]{primary, secondary, defaultOrder};
            default:
                return new OrderSpecifier[]{defaultOrder};
        }
    }
}

