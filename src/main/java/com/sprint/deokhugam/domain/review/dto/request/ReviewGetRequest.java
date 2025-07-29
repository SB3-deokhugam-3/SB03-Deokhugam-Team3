package com.sprint.deokhugam.domain.review.dto.request;

import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.UUID;

public record ReviewGetRequest(
    UUID userId, // 작성자 ID
    UUID bookId, // 도서 ID
    String keyword, // 검색 키워드 (작성자 닉네임 | 내용)
    Object cursor, //  페이지네이션 커서
    Instant after, // 보조 커서(createdAt)
    @Positive
    Integer limit, // 페이지 크기 , default:50
    String orderBy, // 정렬 기준(createdAt | rating)
    String direction  // 정렬 방향( DESC | ASC )
) {

    public ReviewGetRequest {
        if (limit == null) {
            limit = 50;
        }
        if (orderBy == null) {
            orderBy = "createdAt";
        }
        if (direction == null) {
            direction = "ASC";
        }
    }

    public ReviewGetRequest withLimit(Integer newLimit) {
        return new ReviewGetRequest(this.userId, this.bookId, this.keyword, this.cursor, this.after,
            newLimit, this.orderBy, this.direction);
    }
}
