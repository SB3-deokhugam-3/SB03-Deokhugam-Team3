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
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;


@Repository
@RequiredArgsConstructor
@Slf4j
public class BookRepositoryImpl implements BookRepositoryCustom {

    private static final QBook book = QBook.book;
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public List<Book> findBooksWithKeyword(BookSearchRequest request) {
        log.debug("[BookRepository] 키워드 검색 실행 - keyword: {}, orderBy: {}, direction: {}, limit: {}",
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
        log.debug("[BookRepository] 커서 기반 검색 실행 - keyword: {}, cursor: {}, after: {}",
            request.keyword(), request.cursor(), request.after());

        BooleanBuilder whereCondition = new BooleanBuilder()
            .and(book.isDeleted.eq(false))
            .and(keywordCondition(request.keyword()))
            .and(cursorCondition(request.orderBy(), request.direction(), request.cursor(),
                request.after()));

        return queryFactory
            .selectFrom(book)
            .where(whereCondition)
            .orderBy(createOrderSpecifiers(request.orderBy(), request.direction()))
            .limit(request.limit())
            .fetch();
    }

    @Override
    public long countBooksWithKeyword(String keyword) {
        log.debug("[BookRepository] 총 개수 조회 실행 - keyword: {}", keyword);

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

    @Override
    public void hardDeleteBook(UUID bookId) {
        log.info("[BookRepository] 도서 물리 삭제 실행 - bookId: {}", bookId);

        // JPA의 delete 메서드를 사용하면 CASCADE로 자동 삭제
        Book book = entityManager.find(Book.class, bookId);
        if (book != null) {
            entityManager.remove(book);
            log.info("[BookRepository] 도서 및 관련 데이터 삭제 완료 - bookId: {}", bookId);
        } else {
            log.warn("[BookRepository] 삭제할 도서를 찾을 수 없음 - bookId: {}", bookId);
        }
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
    private BooleanExpression cursorCondition(String orderBy, String direction, String cursorValue,
        Instant after) {
        if (cursorValue == null || after == null) {
            return null;
        }

        boolean isAsc = "ASC".equals(direction);

        return switch (orderBy) {
            case "title" ->
                createCursorCondition(book.title, cursorValue, book.createdAt, after, isAsc);
            case "publishedDate" ->
                createCursorCondition(book.publishedDate, LocalDate.parse(cursorValue),
                    book.createdAt, after, isAsc);
            case "rating" ->
                createNumberCursorCondition(book.rating, Double.parseDouble(cursorValue),
                    book.createdAt, after, isAsc);
            case "reviewCount" ->
                createNumberCursorCondition(book.reviewCount, Long.parseLong(cursorValue),
                    book.createdAt, after, isAsc);
            default -> book.createdAt.lt(after);
        };
    }

    /**
     * Comparable 타입 커서 조건 생성 (String, LocalDate 등)
     */
    private <T extends Comparable<? super T>> BooleanExpression createCursorCondition(
        ComparableExpression<T> orderField,
        T cursorValue,
        ComparableExpression<Instant> timeField,
        Instant after,
        boolean isAsc
    ) {
        BooleanExpression primary = isAsc ? orderField.gt(cursorValue) : orderField.lt(cursorValue);
        BooleanExpression secondary = isAsc ? timeField.gt(after) : timeField.lt(after);
        return primary.or(orderField.eq(cursorValue).and(secondary));
    }

    /**
     * Number 타입 커서 조건 생성 (Double, Long 등)
     */
    private <T extends Number & Comparable<? super T>> BooleanExpression createNumberCursorCondition(
        NumberExpression<T> orderField,
        T cursorValue,
        ComparableExpression<Instant> timeField,
        Instant after,
        boolean isAsc
    ) {
        BooleanExpression primary = isAsc ? orderField.gt(cursorValue) : orderField.lt(cursorValue);
        BooleanExpression secondary = isAsc ? timeField.gt(after) : timeField.lt(after);
        return primary.or(orderField.eq(cursorValue).and(secondary));
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
            case "publishedDate" ->
                orderSpecifiers.add(new OrderSpecifier<>(order, book.publishedDate));
            case "rating" -> orderSpecifiers.add(new OrderSpecifier<>(order, book.rating));
            case "reviewCount" ->
                orderSpecifiers.add(new OrderSpecifier<>(order, book.reviewCount));
            default -> orderSpecifiers.add(new OrderSpecifier<>(order, book.title));
        }

        // 보조 정렬 조건 ( 생성시간 )
        orderSpecifiers.add(new OrderSpecifier<>(timeOrder, book.createdAt));

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }
}