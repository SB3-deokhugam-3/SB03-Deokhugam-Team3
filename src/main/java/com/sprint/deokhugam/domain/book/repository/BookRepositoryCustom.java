package com.sprint.deokhugam.domain.book.repository;

import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import java.util.List;

public interface BookRepositoryCustom {
    /**
     * 키워드 검색 + 정렬 (첫 페이지)
     */
    List<Book> findBooksWithKeyword(BookSearchRequest request);

    /**
     * 커서 기반 페이지네이션
     */
    List<Book> findBooksWithKeywordAndCursor(BookSearchRequest request);

    /**
     * 총 개수 조회
     */
    long countBooksWithKeyword(String keyword);

}
