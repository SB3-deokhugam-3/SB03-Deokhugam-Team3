package com.sprint.deokhugam.domain.book.repository;

import com.sprint.deokhugam.domain.book.entity.Book;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {



    /* 키워드 검색 + 정렬 ( 첫 페이지 ) */
    @Query("SELECT b FROM Book b WHERE b.isDeleted = false " +
        "AND (:keyword IS NULL OR :keyword = '' OR " +
        "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "b.isbn LIKE CONCAT('%', :keyword, '%'))")
    List<Book> findBooksWithKeyword(@Param("keyword") String keyword, Pageable pageable);

    /* 커서 기반 페이지네이션 (생성시간 기준) */
    @Query("SELECT b FROM Book b WHERE b.isDeleted = false " +
        "AND (:keyword IS NULL OR :keyword = '' OR " +
        "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "b.isbn LIKE CONCAT('%', :keyword, '%')) " +
        "AND b.createdAt < :after")
    List<Book> findBooksWithKeywordAndCursor(@Param("keyword") String keyword,
                                             @Param("after") Instant after,
                                             Pageable pageable);

    /* 총 개수 조회 */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.isDeleted = false " +
        "AND (:keyword IS NULL OR :keyword = '' OR " +
        "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
        "b.isbn LIKE CONCAT('%', :keyword, '%'))")
    long countBooksWithKeyword(@Param("keyword") String keyword);



}
