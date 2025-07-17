package com.sprint.deokhugam.domain.book.repository;

import com.sprint.deokhugam.domain.book.entity.Book;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID>, BookRepositoryCustom {
    boolean existsByIsbn(String isbn);
}
