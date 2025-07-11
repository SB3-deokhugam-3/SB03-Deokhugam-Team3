package com.sprint.deokhugam.domain.review.dto.request;

import java.time.Instant;
import java.util.UUID;

public record ReviewRequest(
    UUID userId, // 작성자 ID
    UUID bookId, // 도서 ID
    String keyword, // 검색 키워드 (작성자 닉네임 | 내용)
    String orderBy, // 정렬 기준(createdAt | rating)
    String direction, // 정렬 방향( DESC | ASC )
    String cursor, //  페이지네이션 커서
    Instant after, // 보조 커서(createdAt)
    Integer limit, // 페이지 크기
    UUID requestUserId // 요청자 ID , not null
// 추가)header에 Deokhugam-Request-User-ID 넘어옴
) {

}
