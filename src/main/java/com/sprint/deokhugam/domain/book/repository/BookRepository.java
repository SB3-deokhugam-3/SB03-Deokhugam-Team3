package com.sprint.deokhugam.domain.book.repository;

import com.sprint.deokhugam.domain.book.entity.Book;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID>, BookRepositoryCustom {
    boolean existsByIsbn(String isbn);

    @Query(value = "SELECT * FROM books WHERE id = :id AND is_deleted = true", nativeQuery = true)
    Optional<Book> findDeletedById(@Param("id") UUID bookId);

    @Query(value = "SELECT * FROM books WHERE id = :id", nativeQuery = true)
    Optional<Book> findByIdIncludingDeleted(@Param("id") UUID bookId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Book b SET b.reviewCount = (
            SELECT COUNT(r) FROM Review r 
            WHERE r.book.id = b.id AND r.isDeleted = false
        ) 
        WHERE b.id IN :bookIds
        """)
    void recalculateBookCounts(@Param("bookIds") List<UUID> bookIds);
}
