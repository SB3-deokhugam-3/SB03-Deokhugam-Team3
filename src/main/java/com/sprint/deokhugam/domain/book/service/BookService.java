package com.sprint.deokhugam.domain.book.service;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;

public interface BookService {
    /*
     * 도서 목록 조회 ( 키워드 검색 + 커서 페이지네이션 )
     * @param keyword 검색 키워드 ( 제목, 저자, ISBN에서 부분 일치 )
     * @param orderBy 정렬 기준 ( 제목, 출판일, 평점, 리뷰수 )
     * @param direction 정렬 방향 ( ASC, DESC )
     * @param cursor 커서 값 ( 이전 페이지 마지막 요소의 정렬 기준 값 )
     * @param after 이전 페이지 마지막 요소의 생성 시간
     * @param limit 페이지 크기
     * @return 도서 목록 응답 */

    CursorPageResponse<BookDto> getBooks(BookSearchRequest request);
}
