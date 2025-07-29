package com.sprint.deokhugam.domain.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ISBN으로 Naver API에서 도서 정보 조회 시 받은 JSON 파싱을 위한 DTO.
 *
 * @param title       제목
 * @param author      저자
 * @param description 설명
 * @param publisher   출판사
 * @param pubDate     출간일 (yymmdd)
 * @param isbn        ISBN
 * @param image       이미지 URL
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverBookItem(
    String title,
    String author,
    String description,
    String publisher,
    @JsonProperty("pubdate")
    String pubDate,
    String isbn,
    String image
) {

}
