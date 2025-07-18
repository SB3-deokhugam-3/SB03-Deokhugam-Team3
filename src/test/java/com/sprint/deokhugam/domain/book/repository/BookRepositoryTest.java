package com.sprint.deokhugam.domain.book.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.global.config.JpaAuditingConfig;
import com.sprint.deokhugam.global.config.QueryDslConfig;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
@TestPropertySource(properties =  "spring.sql.init.mode=never")
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll(); // DB 초기화

        bookRepository.saveAll(List.of(
            Book.builder()
                .title("토요일이 끝나가는 구나..")
                .author("김현기")
                .description("스프링 부트 정석 가이드")
                .publisher("3팀")
                .publishedDate(LocalDate.of(2023, 6, 1))
                .isbn("9788987654321")
                .rating(4.5)
                .reviewCount(3L)
                .isDeleted(false)
                .build(),
            Book.builder()
                .title("내일은 일요일")
                .author("Testman")
                .description("피곤하다..")
                .publisher("3팀")
                .publishedDate(LocalDate.of(2024, 12, 15))
                .isbn("9788123456789")
                .rating(4.8)
                .reviewCount(10L)
                .isDeleted(false)
                .build(),
            Book.builder()
                .title("비와서 찝찝해;")
                .author("RainMan")
                .description("테스트 도서")
                .publisher("333팀")
                .publishedDate(LocalDate.of(2025, 7, 17))
                .isbn("9781234567890")
                .rating(3.2)
                .reviewCount(15L)
                .isDeleted(false)
                .build(),
            Book.builder()
                .title("삭제된 도서")
                .author("삭제된 저자")
                .description("삭제된 설명")
                .publisher("삭제된 출판사")
                .publishedDate(LocalDate.of(2020, 1, 1))
                .isbn("9780000000000")
                .rating(1.0)
                .reviewCount(1L)
                .isDeleted(true) // 삭제된 도서
                .build()
        ));
    }

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

    @Test
    void 기본_JPA_메서드_findAll() {
        // when
        List<Book> result = bookRepository.findAll();

        // then
        assertThat(result).hasSize(3);
    }


    /** 정렬 기준별 분기 테스트 **/

    @Test
    void 정렬_기준_title_ASC() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "title", "ASC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(3); // 삭제되지 않은 도서만
        assertThat(result.get(0).getTitle()).isEqualTo("내일은 일요일");
        assertThat(result.get(1).getTitle()).isEqualTo("비와서 찝찝해;");
        assertThat(result.get(2).getTitle()).isEqualTo("토요일이 끝나가는 구나..");
    }

    @Test
    void 정렬_기준_title_DESC() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "title", "DESC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitle()).isEqualTo("토요일이 끝나가는 구나..");
        assertThat(result.get(1).getTitle()).isEqualTo("비와서 찝찝해;");
        assertThat(result.get(2).getTitle()).isEqualTo("내일은 일요일");
    }

    @Test
    void 정렬_기준_publishedDate_ASC() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "publishedDate", "ASC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getPublishedDate()).isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(result.get(1).getPublishedDate()).isEqualTo(LocalDate.of(2024, 12, 15));
        assertThat(result.get(2).getPublishedDate()).isEqualTo(LocalDate.of(2025, 7, 17));
    }

    @Test
    void 정렬_기준_publishedDate_DESC() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "publishedDate", "DESC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getPublishedDate()).isEqualTo(LocalDate.of(2025, 7, 17));
        assertThat(result.get(1).getPublishedDate()).isEqualTo(LocalDate.of(2024, 12, 15));
        assertThat(result.get(2).getPublishedDate()).isEqualTo(LocalDate.of(2023, 6, 1));
    }

    @Test
    void 정렬_기준_rating_ASC() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "rating", "ASC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRating()).isEqualTo(3.2);
        assertThat(result.get(1).getRating()).isEqualTo(4.5);
        assertThat(result.get(2).getRating()).isEqualTo(4.8);
    }

    @Test
    void 정렬_기준_rating_DESC() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "rating", "DESC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRating()).isEqualTo(4.8);
        assertThat(result.get(1).getRating()).isEqualTo(4.5);
        assertThat(result.get(2).getRating()).isEqualTo(3.2);
    }

    @Test
    void 정렬_기준_reviewCount_ASC() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "reviewCount", "ASC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getReviewCount()).isEqualTo(3L);
        assertThat(result.get(1).getReviewCount()).isEqualTo(10L);
        assertThat(result.get(2).getReviewCount()).isEqualTo(15L);
    }

    @Test
    void 정렬_기준_reviewCount_DESC() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "reviewCount", "DESC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getReviewCount()).isEqualTo(15L);
        assertThat(result.get(1).getReviewCount()).isEqualTo(10L);
        assertThat(result.get(2).getReviewCount()).isEqualTo(3L);
    }

    @Test
    void 정렬_기준_default_잘못된_값() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "invalidField", "DESC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(3);
        // default case에서 title로 정렬됨
        assertThat(result.get(0).getTitle()).isEqualTo("토요일이 끝나가는 구나..");
        assertThat(result.get(1).getTitle()).isEqualTo("비와서 찝찝해;");
        assertThat(result.get(2).getTitle()).isEqualTo("내일은 일요일");
    }

    /** 키워드 검색 분기 테스트 **/

    @Test
    void 키워드_검색_제목으로_검색() {
        // given
        String keyword = "요일";
        BookSearchRequest request = BookSearchRequest.of(keyword, "title", "DESC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(book -> book.getTitle().contains("요일"));
    }

    @Test
    void 키워드_검색_저자로_검색() {
        // given
        String keyword = "김현기";
        BookSearchRequest request = BookSearchRequest.of(keyword, "title", "DESC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor()).isEqualTo("김현기");
    }

    @Test
    void 키워드_검색_ISBN으로_검색() {
        // given
        String keyword = "9788987654321";
        BookSearchRequest request = BookSearchRequest.of(keyword, "title", "DESC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsbn()).isEqualTo("9788987654321");
    }

    @Test
    void 키워드_검색_키워드_없음_null() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "title", "DESC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(3); // 모든 삭제되지 않은 도서 반환
    }

    @Test
    void 키워드_검색_키워드_없음_빈_문자열() {
        // given
        BookSearchRequest request = BookSearchRequest.of("", "title", "DESC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).hasSize(3); // 모든 삭제되지 않은 도서 반환
    }

    @Test
    void 키워드_검색_일치하는_결과_없음() {
        // given
        String keyword = "존재하지않는키워드";
        BookSearchRequest request = BookSearchRequest.of(keyword, "title", "DESC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).isEmpty();
    }

    /** 커서 기반 페이지네이션 분기 테스트 **/

    @Test
    void 커서_조건_title_기준_커서() {
        // given
        Instant after = Instant.now().minus(1, ChronoUnit.HOURS);
        BookSearchRequest request = BookSearchRequest.of(null, "title", "DESC", "토요일이 끝나가는 구나..", after, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeywordAndCursor(request);

        // then
        assertThat(result).isNotEmpty();
        // B Book Title보다 작은 제목들이 조회되어야 함 (DESC 정렬)
        assertThat(result).allMatch(book -> book.getTitle().compareTo("토요일이 끝나가는 구나..") < 0);
    }

    @Test
    void 커서_조건_publishedDate_기준_커서() {
        // given
        Instant after = Instant.now().minus(1, ChronoUnit.HOURS);
        BookSearchRequest request = BookSearchRequest.of(null, "publishedDate", "DESC", "2025-06-01", after, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeywordAndCursor(request);

        // then
        assertThat(result).isNotEmpty();
        // 2025-06-01보다 이전 날짜들이 조회되어야 함 (DESC 정렬)
        assertThat(result).allMatch(book -> book.getPublishedDate().isBefore(LocalDate.of(2025, 6, 1)));
    }

    @Test
    void 커서_조건_rating_기준_커서() {
        // given
        Instant after = Instant.now().minus(1, ChronoUnit.HOURS);
        BookSearchRequest request = BookSearchRequest.of(null, "rating", "DESC", "4.5", after, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeywordAndCursor(request);

        // then
        assertThat(result).isNotEmpty();
        // 4.5보다 낮은 평점들이 조회되어야 함 (DESC 정렬)
        assertThat(result).allMatch(book -> book.getRating() < 4.5);
    }

    @Test
    void 커서_조건_reviewCount_기준_커서() {
        // given
        Instant after = Instant.now().minus(1, ChronoUnit.HOURS);
        BookSearchRequest request = BookSearchRequest.of(null, "reviewCount", "DESC", "10", after, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeywordAndCursor(request);

        // then
        assertThat(result).isNotEmpty();
        // 10보다 적은 리뷰 수들이 조회되어야 함 (DESC 정렬)
        assertThat(result).allMatch(book -> book.getReviewCount() < 10);
    }

    @Test
    void 커서_조건_커서_값_없음() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "title", "DESC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeywordAndCursor(request);

        // then
        assertThat(result).hasSize(3); // 커서 조건 없이 모든 결과 반환
    }

    @Test
    void 커서_조건_default_분기() {
        // given
        Instant after = Instant.now().minus(1, ChronoUnit.HOURS);
        BookSearchRequest request = BookSearchRequest.of(null, "invalidField", "DESC", "someValue", after, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeywordAndCursor(request);

        // then
        // default case에서 book.createdAt.lt(after) 조건 적용
        assertThat(result).allMatch(book -> book.getCreatedAt().isBefore(after));
    }

    /** 삭제된 도서 필터링 테스트 **/

    @Test
    void 삭제된_도서_필터링() {
        // given
        BookSearchRequest request = BookSearchRequest.of("삭제된", "title", "DESC", null, null, 10);

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(request);

        // then
        assertThat(result).isEmpty(); // 삭제된 도서는 조회되지 않음
    }

    /** 총 개수 조회 테스트 **/
    @Test
    void 총_개수_조회_키워드_있음() {
        // given
        String keyword = "요일";

        // when
        long count = bookRepository.countBooksWithKeyword(keyword);

        // then
        assertThat(count).isEqualTo(2); // 삭제되지 않은 도서 중 "Book"을 포함한 도서 수
    }

    @Test
    void 총_개수_조회_키워드_없음() {
        // given
        String keyword = null;

        // when
        long count = bookRepository.countBooksWithKeyword(keyword);

        // then
        assertThat(count).isEqualTo(3); // 삭제되지 않은 모든 도서 수
    }

    /**  기존 유효성 검증 테스트 **/

    @Test
    void 정렬기준_및_방향_유효성검증_잘못된_값_입력() {
        // given : 잘못된 정렬 기준 및 방향
        String invalidOrderBy = "invalidField";
        String invalidDirection = "invalidDirection";
        BookSearchRequest request = BookSearchRequest.of("test", invalidOrderBy, invalidDirection, null, null, 10);

        // when: validate 메서드 호출
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        // then: 발생한 예외 메시지가 올바른지 확인
        assertThat(exception.getMessage()).isEqualTo("정렬 기준은 title, publishedDate, rating, reviewCount 중 하나여야 합니다.");
    }

    @Test
    void 페이지_크기_유효성_검증_유효하지_않은_값() {
        // given : 잘못된 페이지 크기
        int invalidPageSize = 101;
        BookSearchRequest request = BookSearchRequest.of("test", "title", "DESC", null, null, invalidPageSize);

        // when: validate 메서드 호출
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            request.validate();
        });

        // then: 발생한 예외 메시지가 올바른지 확인
        assertThat(exception.getMessage()).isEqualTo("페이지 크기는 1 이상 100 이하여야 합니다.");
    }

    @Test
    void 도서_물리_삭제_소프트_삭제된_도서만_물리_삭제_가능() {
        // given
        Book softDeletedBook = Book.builder()
            .title("물리 삭제 테스트 도서")
            .author("테스트 저자")
            .description("물리 삭제 테스트용 도서")
            .publisher("테스트 출판사")
            .publishedDate(LocalDate.of(2024, 1, 1))
            .isbn("9999999999999")
            .rating(3.5)
            .reviewCount(5L)
            .isDeleted(true) // 소프트 삭제된 상태
            .build();

        Book savedBook = bookRepository.save(softDeletedBook);
        UUID bookId = savedBook.getId();

        // when
        bookRepository.hardDeleteBook(bookId);

        // then
        Optional<Book> deletedBook = bookRepository.findByIdIncludingDeleted(bookId);
        assertThat(deletedBook).isEmpty();
    }

    @Test
    void 도서_물리_삭제_존재하지_않는_도서_ID() {
        // given
        UUID nonExistentBookId = UUID.randomUUID();

        // when
        Exception exception = null;
        try {
            bookRepository.hardDeleteBook(nonExistentBookId);
        } catch (Exception e) {
            exception = e;
        }

        // then
        assertThat(exception).isNull();
    }

    @Test
    void 소프트_삭제된_도서_포함_조회_findByIdIncludingDeleted() {
        // given
        List<Book> allBooks = bookRepository.findAll();
        Book softDeletedBook = allBooks.stream()
            .filter(book -> book.getTitle().equals("삭제된 도서"))
            .findFirst()
            .orElseThrow();

        UUID deletedBookId = softDeletedBook.getId();

        // when
        Optional<Book> result = bookRepository.findByIdIncludingDeleted(deletedBookId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("삭제된 도서");
        assertThat(result.get().isDeleted()).isTrue();
    }

    @Test
    void 일반_조회에서는_소프트_삭제된_도서_제외_findById() {
        // given
        List<Book> allBooks = bookRepository.findAll();
        Book softDeletedBook = allBooks.stream()
            .filter(book -> book.getTitle().equals("삭제된 도서"))
            .findFirst()
            .orElseThrow();

        UUID deletedBookId = softDeletedBook.getId();

        // when
        Optional<Book> result = bookRepository.findById(deletedBookId);

        // then
        assertThat(result).isEmpty();
    }
}
