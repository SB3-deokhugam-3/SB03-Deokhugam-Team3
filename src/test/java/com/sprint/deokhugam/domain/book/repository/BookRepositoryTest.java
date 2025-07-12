package com.sprint.deokhugam.domain.book.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.global.config.QueryDslConfig;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@DisplayName("BookRepository JPA 테스트")
@EnableJpaAuditing
public class BookRepositoryTest {

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
                .publishedDate(LocalDate.of(2025, 6, 1))
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
                .publishedDate(LocalDate.of(2027, 12, 15))
                .isbn("9788123456789")
                .rating(4.8)
                .reviewCount(10L)
                .isDeleted(false)
                .build()
        ));
    }

    @Test
    @DisplayName("기본 JPA 메서드 - findAll")
    void 기본_JPA_메서드_findAll() {
        // when
        List<Book> result = bookRepository.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("토요일이 끝나가는 구나..");
        assertThat(result.get(1).getTitle()).isEqualTo("내일은 일요일");
    }

    @Test
    @DisplayName("키워드 검색 - findBookWithKeyword")
    void 키워드_검색_findBookWithKeyword() {
        // given
        String keyword = "토요일";

        // when
        List<Book> result = bookRepository.findBooksWithKeyword(new BookSearchRequest(keyword,"title", "DESC", null, null, 10));


        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("토요일");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - findBooksWithKeywordAndCursor")
    void 커서_기반_페이지네이션_findBooksWithKeywordAndCursor() {
        // given
        Instant after = Instant.now().minus(1, ChronoUnit.DAYS); // 적절한 after 값 설정
        BookSearchRequest request = BookSearchRequest.of("일요일", "title", "DESC", null, after, 1);

        // when
        List<Book> result = bookRepository.findBooksWithKeywordAndCursor(request);

        // then
        assertThat(result).isNotEmpty(); // 결과가 비어 있지 않아야 함
        assertThat(result).hasSize(1);  // 예상 크기 확인
        assertThat(result.get(0).getTitle()).isEqualTo("내일은 일요일"); // 타이틀 확인
    }

    @Test
    @DisplayName("도서 총 개수 조회 ")
    void 도서_총_개수_조회_countBooksWithKeyword() {
        // given
        String keyword = "토요일";

        // when
        long count = bookRepository.countBooksWithKeyword(keyword);

        // then
         assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("정렬기준 및 방향 유효성 검증 - 잘못된 값 입력")
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
    @DisplayName("페이지 크기 유효성 검증 - 유효하지 않은 값")
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
}
