package com.sprint.deokhugam.domain.review.dto.request;

import lombok.Getter;

@Getter
public enum ReviewFeature {
    HARD_DELETE("리뷰 하드 삭제 실패"),
    SOFT_DELETE("리뷰 소프트 삭제 실패"),
    UPDATE("리뷰 수정 실패");

    private final String message;

    ReviewFeature(String message) {
        this.message = message;
    }
}
