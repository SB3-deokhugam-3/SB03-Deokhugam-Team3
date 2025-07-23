package com.sprint.deokhugam.domain.popularbook.repository;

import static com.sprint.deokhugam.fixture.BookFixture.createBookEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.popularbook.dto.data.PopularBookDto;
import com.sprint.deokhugam.domain.popularbook.dto.request.PopularBookGetRequest;
import com.sprint.deokhugam.domain.popularbook.entity.PopularBook;
import com.sprint.deokhugam.global.config.JpaAuditingConfig;
import com.sprint.deokhugam.global.config.QueryDslConfig;
import com.sprint.deokhugam.global.period.PeriodType;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
@ActiveProfiles("test")
@DisplayName("PopularBookReposiroty 테스트")
class PopularBookRepositoryTest {

    @Autowired
    private PopularBookRepository popularBookRepository;

    @Autowired
    private TestEntityManager em;

    private Book book2;

    @BeforeEach
    void setUp() {
        // given
        Book book1 = createBookEntity("test book", "test author", "description1",
            "test publisher", LocalDate.of(2022, 2, 2), "1234565432177",
            null, 4.5, 10L);

        book2 = createBookEntity("test book2", "test author2", "description2",
            "test publisher2", LocalDate.of(2023, 3, 3), "1234565432188",
            null, 4.0, 5L);

        book2 = createBookEntity("test book2", "test author2", "description2",
            "test publisher2", LocalDate.of(2023, 3, 3), "1234565432188",
            null, 4.0, 5L);

        Book book3 = createBookEntity("test book3", "test author3", "description3",
            "test publisher2", LocalDate.of(2024, 4, 4), "1234565432199",
            null, 3.0, 3L);

        book1 = em.persistAndFlush(book1);
        book2 = em.persistAndFlush(book2);
        book3 = em.persistAndFlush(book3);

        PopularBook pb1 = PopularBook.builder()
            .period(PeriodType.DAILY)
            .rank(2L)
            .score(3.0)
            .reviewCount(3L)
            .rating(3.0)
            .book(book1)
            .build();

        PopularBook pb2 = PopularBook.builder()
            .period(PeriodType.DAILY)
            .rank(1L)
            .score(4.4)
            .reviewCount(5L)
            .rating(4.0)
            .book(book2)
            .build();

        PopularBook pb3 = PopularBook.builder()
            .period(PeriodType.DAILY)
            .rank(3L)
            .score(2.2)
            .reviewCount(1L)
            .rating(3.0)
            .book(book3)
            .build();

        em.persistAndFlush(pb1);
        em.persistAndFlush(pb2);
        em.persistAndFlush(pb3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ASC, DESC"})
    void 인기_도서_목록_조회_테스트(String direction) {

        // given
        PopularBookGetRequest request = PopularBookGetRequest.builder()
            .period(PeriodType.DAILY)
            .direction(direction)
            .limit(10)
            .build();

        // when
        List<PopularBookDto> result = popularBookRepository.findAllByRequest(request);

        // then
        assertEquals(3, result.size());
        if (direction.equals("ASC")) {
            assertEquals(1L, result.get(0).getRank());
            assertEquals(2L, result.get(1).getRank());
            assertEquals("test book2", result.get(0).getTitle());
        } else if (direction.equals("DESC")) {
            assertEquals(3L, result.get(0).getRank());
            assertEquals(2L, result.get(1).getRank());
            assertEquals("test book3", result.get(0).getTitle());
        }
    }

    @Test
    void 커서가_있는_경우_인기_도서_목록_조회_테스트() {

        // given
        PopularBookGetRequest request = PopularBookGetRequest.builder()
            .period(PeriodType.DAILY)
            .direction("ASC")
            .cursor("2")
            .after(book2.getCreatedAt().toString())
            .limit(2)
            .build();

        // when
        List<PopularBookDto> result = popularBookRepository.findAllByRequest(request);

        // then
        assertEquals(1, result.size());
        assertEquals("test book3", result.get(0).getTitle());
    }

    @Test
    void 잘못된_커서_형식이_들어오면_예외가_발생한다() {

        // given
        PopularBookGetRequest request = PopularBookGetRequest.builder()
            .period(PeriodType.DAILY)
            .direction("ASC")
            .cursor("2L")
            .after(book2.getCreatedAt().toString())
            .limit(2)
            .build();

        // when
        Throwable thrown = catchThrowable(() -> popularBookRepository.findAllByRequest(request));

        // then
        // NumberFormatException을 스프링 예외인 InvalidDataAccessApiUsageException으로 Wrapping
        assertThat(thrown)
            .isInstanceOf(InvalidDataAccessApiUsageException.class);
    }

    @Test
    void 잘못된_after_형식이_들어오면_예외가_발생한다() {

        // given
        PopularBookGetRequest request = PopularBookGetRequest.builder()
            .period(PeriodType.DAILY)
            .direction("ASC")
            .cursor("2")
            .after("Invalid Time")
            .limit(2)
            .build();

        // when
        Throwable thrown = catchThrowable(() -> popularBookRepository.findAllByRequest(request));

        // then
        assertThat(thrown)
            .isInstanceOf(DateTimeParseException.class);
    }
}