package com.sprint.deokhugam.domain.book.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.entity.QBook;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BookRepositoryImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QBook book = QBook.book;

    @Override
    public List<Book> findBooksWithKeyword(BookSearchRequest request) {
        log.debug("키워드 검색 실행 - keyword: {}, orderBy: {}, direction: {}, limit: {}",
            request.keyword(), request.orderBy(), request.direction(), request.limit());

        return queryFactory
            .selectFrom(book)
            .where(
                book.isDeleted.eq(false),
                keywordCondition(request.keyword())
            )
            .orderBy(createOrderSpecifiers(request.orderBy(), request.direction()))
            .limit(request.limit())
            .fetch();
    }

    @Override
    public List<Book> findBooksWithKeywordAndCursor(BookSearchRequest request) {
        log.debug("커서 기반 검색 실행 - keyword: {}, cursor: {}, after: {}",
            request.keyword(), request.cursor(), request.after());

        BooleanBuilder whereCondition = new BooleanBuilder()
            .and(book.isDeleted.eq(false))
            .and(keywordCondition(request.keyword()))
            .and(cursorCondition(request.orderBy(), request.direction(), request.cursor(), request.after()));

        return queryFactory
            .selectFrom(book)
            .where(whereCondition)
            .orderBy(createOrderSpecifiers(request.orderBy(), request.direction()))
            .limit(request.limit())
            .fetch();
    }

    @Override
    public long countBooksWithKeyword(String keyword) {
        log.debug("총 개수 조회 실행 - keyword: {}", keyword);

        Long count = queryFactory
            .select(book.count())
            .from(book)
            .where(
                book.isDeleted.eq(false),
                keywordCondition(keyword)
            )
            .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 키워드 검색 조건 생성
     */
    private BooleanExpression keywordCondition(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return book.title.containsIgnoreCase(keyword)
            .or(book.author.containsIgnoreCase(keyword))
            .or(book.isbn.contains(keyword));
    }

    /**
     * 커서 조건 생성
     */
    private BooleanExpression cursorCondition(String orderBy, String direction, String cursorValue, Instant after) {
        if (cursorValue == null || after == null) {
            return null;
        }

        boolean isAsc = "ASC".equals(direction);

        return switch (orderBy) {
            case "title" -> createStringCursorCondition(book.title, cursorValue, book.createdAt, after, isAsc);
            case "publishedDate" -> createLocalDateCursorCondition(book.publishedDate, LocalDate.parse(cursorValue), book.createdAt, after, isAsc);
            case "rating" -> createDoubleCursorCondition(book.rating, Double.parseDouble(cursorValue), book.createdAt, after, isAsc);
            case "reviewCount" -> createLongCursorCondition(book.reviewCount, Long.parseLong(cursorValue), book.createdAt, after, isAsc);
            default -> book.createdAt.lt(after);
        };
    }

    /**
     * String 타입 커서 조건 생성
     */
    private BooleanExpression createStringCursorCondition(
        ComparableExpression<String> orderField, String cursorValue,
        ComparableExpression<Instant> timeField, Instant after, boolean isAsc) {

        if (isAsc) {
            return orderField.gt(cursorValue)
                .or(orderField.eq(cursorValue).and(timeField.gt(after)));
        } else {
            return orderField.lt(cursorValue)
                .or(orderField.eq(cursorValue).and(timeField.lt(after)));
        }
    }

    /**
     * LocalDate 타입 커서 조건 생성
     */
    private BooleanExpression createLocalDateCursorCondition(
        ComparableExpression<LocalDate> orderField, LocalDate cursorValue,
        ComparableExpression<Instant> timeField, Instant after, boolean isAsc) {

        if (isAsc) {
            return orderField.gt(cursorValue)
                .or(orderField.eq(cursorValue).and(timeField.gt(after)));
        } else {
            return orderField.lt(cursorValue)
                .or(orderField.eq(cursorValue).and(timeField.lt(after)));
        }
    }

    /**
     * Double 타입 커서 조건 생성
     */
    private BooleanExpression createDoubleCursorCondition(
        NumberExpression<Double> orderField, Double cursorValue,
        ComparableExpression<Instant> timeField, Instant after, boolean isAsc) {

        if (isAsc) {
            return orderField.gt(cursorValue)
                .or(orderField.eq(cursorValue).and(timeField.gt(after)));
        } else {
            return orderField.lt(cursorValue)
                .or(orderField.eq(cursorValue).and(timeField.lt(after)));
        }
    }

    /**
     * Long 타입 커서 조건 생성
     */
    private BooleanExpression createLongCursorCondition(
        NumberExpression<Long> orderField, Long cursorValue,
        ComparableExpression<Instant> timeField, Instant after, boolean isAsc) {

        if (isAsc) {
            return orderField.gt(cursorValue)
                .or(orderField.eq(cursorValue).and(timeField.gt(after)));
        } else {
            return orderField.lt(cursorValue)
                .or(orderField.eq(cursorValue).and(timeField.lt(after)));
        }
    }

    /**
     * 정렬 조건 생성
     */
    private OrderSpecifier<?>[] createOrderSpecifiers(String orderBy, String direction) {
        Order order = "ASC".equals(direction) ? Order.ASC : Order.DESC;
        Order timeOrder = Order.DESC; // 생성시간은 항상 최신순

        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        // 주 정렬 조건
        switch (orderBy) {
            case "title" -> orderSpecifiers.add(new OrderSpecifier<>(order, book.title));
            case "publishedDate" -> orderSpecifiers.add(new OrderSpecifier<>(order, book.publishedDate));
            case "rating" -> orderSpecifiers.add(new OrderSpecifier<>(order, book.rating));
            case "reviewCount" -> orderSpecifiers.add(new OrderSpecifier<>(order, book.reviewCount));
            default -> orderSpecifiers.add(new OrderSpecifier<>(order, book.title));
        }

        // 보조 정렬 조건 (생성시간)
        orderSpecifiers.add(new OrderSpecifier<>(timeOrder, book.createdAt));

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }
}
