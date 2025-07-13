package com.sprint.deokhugam.domain.book.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.global.config.JpaAuditingConfig;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void 책을_등록하고_조회할_수_있어야_한다() {

        // given
        Book bookEntity =  Book.builder()
            .title("test book")
            .author("test author")
            .description("test description")
            .publisher("test publisher")
            .publishedDate(LocalDate.now())
            .isbn("1234567890123")
            .rating(0.0)
            .reviewCount(0L)
            .isDeleted(false)
            .build();

        // when
        Book savedBook = bookRepository.save(bookEntity);

        // then
        Optional<Book> foundBook = bookRepository.findById(savedBook.getId());
        assertTrue(foundBook.isPresent(), "책이 등록되어야 한다.");
        assertEquals("test book", savedBook.getTitle());
        assertEquals("1234567890123", savedBook.getIsbn());
    }

    @Test
    void 존재하지_않는_isbn_여부를_확인할_수_있어야_한다() {

        // when
        boolean exist = bookRepository.existsByIsbn("1234567890111");

        // then
        assertFalse(exist);
    }
}