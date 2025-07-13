package com.sprint.deokhugam.domain.review.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.review.dto.request.ReviewRequest;
import com.sprint.deokhugam.domain.review.entity.QReview;
import com.sprint.deokhugam.domain.review.entity.Review;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomReviewRepositoryImpl implements CustomReviewRepository {

    private final JPAQueryFactory queryFactory;

    public List<Review> findAll(ReviewRequest params) {
        QReview review = QReview.review;
        String direction = params.getDirection();
        String orderBy = params.getOrderBy();

        BooleanBuilder whereCondition = new BooleanBuilder();
        BooleanBuilder cursorCondition = new BooleanBuilder();

        whereCondition.and(filterByIdAndKeyword(review, params));

        //Q. after만 오는 경우는 의미없음
        boolean isCursorExisted = params.getCursor() != null;
        if (isCursorExisted) {
            cursorCondition = orderBy.equals("createdAt") ? filterByCreatedAt(review, params)
                : filterByRating(review, params);
        }

        return queryFactory
            .selectFrom(review)
            .where(whereCondition, cursorCondition)
            .orderBy(getOrderSpecifiers(review, orderBy, direction))
            .limit(params.getLimit())
            .fetch();
    }

    public Long countAllByFilterCondition(ReviewRequest params) {
        QReview review = QReview.review;
        String orderBy = params.getOrderBy();

        BooleanBuilder whereCondition = new BooleanBuilder();
        BooleanBuilder cursorCondition = new BooleanBuilder();

        whereCondition.and(filterByIdAndKeyword(review, params));

        //Q. after만 오는 경우는 의미없음
        boolean isCursorExisted = params.getCursor() != null;
        if (isCursorExisted) {
            cursorCondition = orderBy.equals("createdAt") ? filterByCreatedAt(review, params)
                : filterByRating(review, params);
        }

        return queryFactory
            .select(review.count())
            .from(review)
            .where(whereCondition, cursorCondition)
            .fetchOne();

    }

    private BooleanBuilder filterByIdAndKeyword(QReview review, ReviewRequest params) {
        BooleanBuilder whereCondition = new BooleanBuilder();
        String keyword = params.getKeyword();
        UUID userId = params.getUserId();
        UUID bookId = params.getBookId();

        if (userId != null) {
            whereCondition.and(review.user.id.eq(userId));
        }
        if (bookId != null) {
            whereCondition.and(review.book.id.eq(bookId));
        }
        /* keyword 조건 */
        if (keyword != null) {
            whereCondition.and(
                review.user.nickname.contains(keyword)
                    .or(review.book.title.contains(keyword)
                        .or(review.book.description.contains(keyword)))
            );

        }

        return whereCondition;
    }

    private BooleanBuilder filterByCreatedAt(QReview review, ReviewRequest params) {
        BooleanBuilder whereCondition = new BooleanBuilder();
        String direction = params.getDirection();
        Instant cursor = Instant.parse(params.getCursor().toString());
        log.info("cursor = {}", Instant.parse(params.getCursor().toString()));
        Instant after = params.getAfter();
        // Q. after 넣는게 의미가 있나?
        if (direction.equals("DESC")) {
            whereCondition
                .or(review.createdAt.loe(cursor));
        } else {
            whereCondition
                .or(review.createdAt.goe(cursor));
        }

        return whereCondition;

    }

    private BooleanBuilder filterByRating(QReview review, ReviewRequest params) {
        BooleanBuilder whereCondition = new BooleanBuilder();
        String direction = params.getDirection();
        Double cursor = Double.valueOf(params.getCursor().toString());
        Instant after = params.getAfter();

        if (direction.equals("DESC")) {
            whereCondition
                .or(review.rating.lt(cursor))
                .or(review.rating.eq(cursor).and(review.createdAt.lt(after)));
        } else {
            whereCondition
                .or(review.rating.gt(cursor))
                .or(review.rating.eq(cursor).and(review.createdAt.gt(after)));
        }

        return whereCondition;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(QReview review, String orderBy,
        String direction) {
        log.info("orderBy ={}, direction={}", orderBy, direction);
        OrderSpecifier<?> primary;
        OrderSpecifier<?> secondary;
        // 다른 order 조건을 거치고도 정렬이 고정되지 않는다면, id값으로 최종정렬
        OrderSpecifier<?> defaultOrder =
            direction.equals("DESC") ? review.id.desc() : review.id.asc();
        switch (orderBy) {
            case "createdAt":
                primary =
                    direction.equals("DESC") ? review.createdAt.desc() : review.createdAt.asc();
                return new OrderSpecifier[]{primary, defaultOrder};
            case "rating":
                primary = direction.equals("DESC") ? review.rating.desc() : review.rating.asc();
                secondary =
                    direction.equals("DESC") ? review.createdAt.desc() : review.createdAt.asc();
                return new OrderSpecifier[]{primary, secondary, defaultOrder};
            default:
                return new OrderSpecifier[]{defaultOrder};
        }
    }
}

