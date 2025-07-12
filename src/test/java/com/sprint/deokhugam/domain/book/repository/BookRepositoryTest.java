package com.sprint.deokhugam.domain.book.repository;

import com.sprint.deokhugam.domain.book.config.TestJpaConfig;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.global.config.QueryDslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, TestJpaConfig.class})
@ActiveProfiles("test")
@DisplayName("BookRepository JPA 기본 기능 테스트")
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private List<Book> testBooks;

    @BeforeEach
    void setUp() {
        testBooks = createAndSaveTestBooks();
    }

    @Test
    @DisplayName("기본 JPA 메서드 - findAll")
    void 기본_JPA_메서드_findAll() {
        // when
        List<Book> result = bookRepository.findAll();

        // then
        assertThat(result).hasSize(5);
        assertThat(result.stream().allMatch(book -> !book.isDeleted())).isTrue();
    }

    @Test
    @DisplayName("기본 JPA 메서드 - findById")
    void 기본_JPA_메서드_findById() {
        // given
        Book savedBook = testBooks.get(0);

        // when
        Optional<Book> result = bookRepository.findById(savedBook.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Super Hot Day");
        assertThat(result.get().getAuthor()).isEqualTo("김현기");
    }

    @Test
    @DisplayName("기본 JPA 메서드 - save")
    void 기본_JPA_메서드_save() {
        // given
        Book newBook = Book.builder()
            .title("새로운 도서")
            .author("새로운 저자")
            .description("새로운 설명")
            .publisher("새로운 출판사")
            .publishedDate(LocalDate.now())
            .isbn("1234567890123")
            .rating(3.5)
            .reviewCount(10L)
            .build();

        // when
        Book savedBook = bookRepository.save(newBook);

        // then
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo("새로운 도서");
        assertThat(savedBook.getCreatedAt()).isNotNull();
        assertThat(savedBook.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("기본 JPA 메서드 - count")
    void 기본_JPA_메서드_count() {
        // when
        long count = bookRepository.count();

        // then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("기본 JPA 메서드 - delete")
    void 기본_JPA_메서드_delete() {
        // given
        Book book = testBooks.get(0);
        var bookId = book.getId();

        // when
        bookRepository.delete(book);
        entityManager.flush();

        // then
        assertThat(bookRepository.findById(bookId)).isEmpty();
    }

    @Test
    @DisplayName("기본 JPA 메서드 - existsById")
    void 기본_JPA_메서드_existsById() {
        // given
        Book book = testBooks.get(0);

        // when
        boolean exists = bookRepository.existsById(book.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("기본 JPA 메서드 - deleteById")
    void 기본_JPA_메서드_deleteById() {
        // given
        Book book = testBooks.get(0);
        var bookId = book.getId();

        // when
        bookRepository.deleteById(bookId);
        entityManager.flush();

        // then
        assertThat(bookRepository.findById(bookId)).isEmpty();
    }

    @Test
    @DisplayName("기본 JPA 메서드 - saveAll")
    void 기본_JPA_메서드_saveAll() {
        // given
        List<Book> newBooks = List.of(
            Book.builder()
                .title("테스트 도서 1")
                .author("테스트 저자 1")
                .description("테스트 설명 1")
                .publisher("테스트 출판사 1")
                .publishedDate(LocalDate.now())
                .isbn("1111111111111")
                .rating(4.0)
                .reviewCount(50L)
                .build(),
            Book.builder()
                .title("테스트 도서 2")
                .author("테스트 저자 2")
                .description("테스트 설명 2")
                .publisher("테스트 출판사 2")
                .publishedDate(LocalDate.now())
                .isbn("2222222222222")
                .rating(3.5)
                .reviewCount(30L)
                .build()
        );

        // when
        List<Book> savedBooks = bookRepository.saveAll(newBooks);

        // then
        assertThat(savedBooks).hasSize(2);
        assertThat(savedBooks.stream().allMatch(book -> book.getId() != null)).isTrue();
    }

    private List<Book> createAndSaveTestBooks() {
        List<Book> books = List.of(
            Book.builder()
                .title("Super Hot Day")
                .author("김현기")
                .description("덥다 더워 덥다 더워")
                .publisher("3팀")
                .publishedDate(LocalDate.of(2023, 1, 1))
                .isbn("9788123456789")
                .rating(4.5)
                .reviewCount(100L)
                .build(),
            Book.builder()
                .title("Spring Boot Guide")
                .author("박스프링")
                .description("스프링 부트 가이드")
                .publisher("웹출판사")
                .publishedDate(LocalDate.of(2023, 6, 1))
                .isbn("9788987654321")
                .rating(4.8)
                .reviewCount(200L)
                .build(),
            Book.builder()
                .title("Database Design")
                .author("이데이터")
                .description("데이터베이스 설계")
                .publisher("DB출판사")
                .publishedDate(LocalDate.of(2023, 3, 1))
                .isbn("9788555666777")
                .rating(4.2)
                .reviewCount(50L)
                .build(),
            Book.builder()
                .title("고급 자바 프로그래밍")
                .author("최고수")
                .description("고급 자바 프로그래밍 기법")
                .publisher("전문출판사")
                .publishedDate(LocalDate.of(2023, 9, 1))
                .isbn("9788444555666")
                .rating(4.9)
                .reviewCount(300L)
                .build(),
            Book.builder()
                .title("알고리즘 입문")
                .author("정알고")
                .description("알고리즘 기초부터 응용까지")
                .publisher("알고출판사")
                .publishedDate(LocalDate.of(2023, 12, 1))
                .isbn("9788777888999")
                .rating(4.3)
                .reviewCount(150L)
                .build()
        );

        return books.stream()
            .map(book -> entityManager.persistAndFlush(book))
            .toList();
    }
}