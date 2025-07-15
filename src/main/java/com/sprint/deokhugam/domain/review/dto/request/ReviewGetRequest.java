package com.sprint.deokhugam.domain.review.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class ReviewGetRequest {

    private UUID userId; // 작성자 ID
    private UUID bookId; // 도서 ID
    private String keyword; // 검색 키워드 (작성자 닉네임 | 내용)
    private String orderBy; // 정렬 기준(createdAt | rating)
    private String direction; // 정렬 방향( DESC | ASC )
    private Object cursor; //  페이지네이션 커서
    private Instant after; // 보조 커서(createdAt)
    @Positive
    private Integer limit = 50; // 페이지 크기 , default:50
    @NotNull
    private UUID requestUserId; // 요청자 ID , not null
}
