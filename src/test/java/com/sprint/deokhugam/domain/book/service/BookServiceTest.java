package com.sprint.deokhugam.domain.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookSearchRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.book.mapper.BookMapper;
import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService 테스트")
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private List<Book> testBooks;
    private List<BookDto> testBookDtos;

    @BeforeEach
    void setUp() {
        testBooks = createTestBooks();
        testBookDtos = createTestBookDtos();
    }

    @Test
    @DisplayName("첫 페이지 조회 - 키워드 검색 없이")
    void 키워드_없이_첫_페이지_조회() {
        //given
        BookSearchRequest request = BookSearchRequest.of(null, "title", "DESC",null,null,10);
        given(bookRepository.findBooksWithKeyword(any(BookSearchRequest.class)))
            .willReturn(testBooks.subList(0, 3));
        given(bookRepository.countBooksWithKeyword(any()))
            .willReturn(15L);
        given(bookMapper.toDto(any(Book.class)))
            .willReturn(testBookDtos.get(0), testBookDtos.get(1), testBookDtos.get(2));

        //when
        CursorPageResponse<BookDto> response = bookService.getBooks(request);

        //then
        assertThat(response.content()).hasSize(3);
        assertThat(response.totalElements()).isEqualTo(15L);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextAfter()).isNull();
        verify(bookRepository).findBooksWithKeyword(any(BookSearchRequest.class));
        verify(bookRepository).countBooksWithKeyword(any());
    }

    @Test
    @DisplayName("첫 페이지 조회 - 키워드 검색 포함")
    void 키워드_포함_첫_페이지_조회() {
        // given
        BookSearchRequest request = BookSearchRequest.of("Hot", "title", "DESC", null, null, 5);
        List<Book> searchResults = testBooks.subList(0, 3);
        given(bookRepository.findBooksWithKeyword(any(BookSearchRequest.class)))
            .willReturn(searchResults);
        given(bookRepository.countBooksWithKeyword("Hot"))
            .willReturn(3L);
        given(bookMapper.toDto(any(Book.class)))
            .willReturn(testBookDtos.get(0), testBookDtos.get(1), testBookDtos.get(2));

        // when
        CursorPageResponse<BookDto> response = bookService.getBooks(request);

        // then
        assertThat(response.content()).hasSize(3);
        assertThat(response.totalElements()).isEqualTo(3L);
        assertThat(response.hasNext()).isFalse();
        verify(bookRepository).findBooksWithKeyword(any(BookSearchRequest.class));
        verify(bookRepository).countBooksWithKeyword("Hot");
    }

    @Test
    @DisplayName("커서 기반 페이지네이션 - 다음 페이지 존재")
    void 커서_기반_페이지네이션_다음_페이지_존재() {
        // given
        Instant after = Instant.now().minusSeconds(3600);
        BookSearchRequest request = BookSearchRequest.of("Super", "title", "DESC", "Java Programming", after, 2);
        List<Book> searchResults = testBooks.subList(0, 3); // limit+1 = 3개 조회
        given(bookRepository.findBooksWithKeywordAndCursor(any(BookSearchRequest.class)))
            .willReturn(searchResults);
        given(bookRepository.countBooksWithKeyword("Super"))
            .willReturn(5L);
        given(bookMapper.toDto(any(Book.class)))
            .willReturn(testBookDtos.get(0), testBookDtos.get(1));

        // when
        CursorPageResponse<BookDto> response = bookService.getBooks(request);

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.totalElements()).isEqualTo(5L);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
        assertThat(response.nextAfter()).isNotNull();
        verify(bookRepository).findBooksWithKeywordAndCursor(any(BookSearchRequest.class));
    }

    @Test
    @DisplayName("다양한 정렬 기준 테스트 - 평점순")
    void 평점순_정렬_테스트() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "rating", "DESC", null, null, 5);
        given(bookRepository.findBooksWithKeyword(any(BookSearchRequest.class)))
            .willReturn(testBooks);
        given(bookRepository.countBooksWithKeyword(any()))
            .willReturn(3L);
        given(bookMapper.toDto(any(Book.class)))
            .willReturn(testBookDtos.get(0), testBookDtos.get(1), testBookDtos.get(2));

        // when
        CursorPageResponse<BookDto> response = bookService.getBooks(request);

        // then
        assertThat(response.content()).hasSize(3);
        verify(bookRepository).findBooksWithKeyword(any(BookSearchRequest.class));
    }

    @Test
    @DisplayName("유효하지 않은 정렬 기준 - 예외 발생")
    void 유효하지_않은_정렬_기준_예외_발생() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "invalid", "DESC", null, null, 5);

        // when & then
        assertThatThrownBy(() -> bookService.getBooks(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("정렬 기준은 title, publishedDate, rating, reviewCount 중 하나여야 합니다.");
    }

    @Test
    @DisplayName("유효하지 않은 페이지 크기 - 예외 발생")
    void 유효하지_않은_페이지_크기_예외_발생() {
        // given
        BookSearchRequest request = BookSearchRequest.of(null, "title", "DESC", null, null, 101);

        // when & then
        assertThatThrownBy(() -> bookService.getBooks(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("페이지 크기는 1 이상 100 이하여야 합니다.");
    }



    private List<Book> createTestBooks() {
        Instant now = Instant.now();

        // 첫 번째 Book 생성 및 createdAt 설정
        Book book1 = Book.builder()
            .title("Super Hot Day")
            .author("김현기")
            .description("덥다 더워 덥다 더워")
            .publisher("3팀")
            .publishedDate(LocalDate.of(2023, 1, 1))
            .isbn("9788123456789")
            .rating(4.5)
            .reviewCount(100L)
            .build();
        // ReflectionTestUtils를 사용하여 BaseEntity의 필드 설정
        ReflectionTestUtils.setField(book1, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(book1, "createdAt", now.minusSeconds(3600));
        ReflectionTestUtils.setField(book1, "updatedAt", now.minusSeconds(1800));

        // 두 번째 Book 생성 및 createdAt 설정
        Book book2 = Book.builder()
            .title("Spring Boot Guide")
            .author("박스프링")
            .description("스프링 부트 가이드")
            .publisher("웹출판사")
            .publishedDate(LocalDate.of(2023, 6, 1))
            .isbn("9788987654321")
            .rating(4.8)
            .reviewCount(200L)
            .build();
        ReflectionTestUtils.setField(book2, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(book2, "createdAt", now.minusSeconds(7200));
        ReflectionTestUtils.setField(book2, "updatedAt", now.minusSeconds(3600));

        // 세 번째 Book 생성 및 createdAt 설정
        Book book3 = Book.builder()
            .title("Database Design")
            .author("이데이터")
            .description("데이터베이스 설계")
            .publisher("DB출판사")
            .publishedDate(LocalDate.of(2023, 3, 1))
            .isbn("9788555666777")
            .rating(4.2)
            .reviewCount(50L)
            .build();
        ReflectionTestUtils.setField(book3, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(book3, "createdAt", now.minusSeconds(10800));
        ReflectionTestUtils.setField(book3, "updatedAt", now.minusSeconds(5400));

        return List.of(book1, book2, book3);
    }

    private List<BookDto> createTestBookDtos() {
        Instant now = Instant.now();
        return List.of(
            new BookDto(
                UUID.randomUUID(),
                "Super Hot Day",
                "김현기",
                "덥다 더워 덥다 더워",
                "3팀",
                LocalDate.of(2023, 1, 1),
                "9788123456789",
                null,
                100L,
                4.5,
                now.minusSeconds(3600),
                now.minusSeconds(1800)
            ),
            new BookDto(
                UUID.randomUUID(),
                "Spring Boot Guide",
                "박스프링",
                "스프링 부트 가이드",
                "웹출판사",
                LocalDate.of(2023, 6, 1),
                "9788987654321",
                null,
                200L,
                4.8,
                now.minusSeconds(7200),
                now.minusSeconds(3600)
            ),
            new BookDto(
                UUID.randomUUID(),
                "Database Design",
                "이데이터",
                "데이터베이스 설계",
                "DB출판사",
                LocalDate.of(2023, 3, 1),
                "9788555666777",
                null,
                50L,
                4.2,
                now.minusSeconds(10800),
                now.minusSeconds(5400)
            )
        );
    }
}
