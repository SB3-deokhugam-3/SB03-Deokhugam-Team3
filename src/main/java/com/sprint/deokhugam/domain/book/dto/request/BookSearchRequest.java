package com.sprint.deokhugam.domain.book.dto.request;

import java.time.Instant;
import java.util.List;

public record BookSearchRequest(String keyword,
                                String orderBy,
                                String direction,
                                String cursor,
                                Instant after,
                                Integer limit) {
    // 기본값을 적용한 정적 팩토리 메서드
    public static BookSearchRequest of(String keyword, String orderBy, String direction,
        String cursor, Instant after, Integer limit) {
        return new BookSearchRequest(
            keyword,
            orderBy != null ? orderBy : "title",
            direction != null ? direction : "DESC",
            cursor,
            after,
            limit != null ? limit : 50
        );
    }

    // 유효성 검증을 위한 메서드들
    public BookSearchRequest validate() {
        validateSortParameters();
        validatePageSize();
        return this;
    }

    private static final List<String> VALID_ORDER_BY = List.of("title", "publishedDate", "rating", "reviewCount");
    private static final List<String> VALID_DIRECTIONS = List.of("ASC", "DESC");

    private void validateSortParameters() {
        if (!VALID_ORDER_BY.contains(orderBy())) {
            throw new IllegalArgumentException("정렬 기준은 title, publishedDate, rating, reviewCount 중 하나여야 합니다.");
        }

        if (!VALID_DIRECTIONS.contains(direction())) {
            throw new IllegalArgumentException("정렬 방향은 ASC 또는 DESC여야 합니다.");
        }
    }

    private void validatePageSize() {
        if (limit() < 1 || limit() > 100) {
            throw new IllegalArgumentException("페이지 크기는 1 이상 100 이하여야 합니다.");
        }
    }

    // 커서 기반 페이지네이션 여부 확인
    public boolean hasCursor() {
        return cursor != null && after != null;
    }
}
