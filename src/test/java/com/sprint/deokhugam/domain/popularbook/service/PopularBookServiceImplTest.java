package com.sprint.deokhugam.domain.popularbook.service;

import static com.sprint.deokhugam.fixture.BookFixture.createBookEntity;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.popularbook.dto.data.PopularBookDto;
import com.sprint.deokhugam.domain.popularbook.dto.request.PopularBookGetRequest;
import com.sprint.deokhugam.domain.popularbook.entity.PopularBook;
import com.sprint.deokhugam.domain.popularbook.repository.PopularBookRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.period.PeriodType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PopularBookService 단위 테스트")
class PopularBookServiceImplTest {

    @InjectMocks
    private PopularBookServiceImpl popularBookService;

    @Mock
    private PopularBookRepository popularBookRepository;

    @BeforeEach
    void setUp() {
        Book book1 = createBookEntity("test book1", "test author1", "description1",
            "test publisher", LocalDate.of(2022, 2, 2), "6789123450432", null,
            4.5, 10L);
        Book book2 = createBookEntity("test book2", "test author2", "description2",
            "test publisher", LocalDate.of(2023, 3, 3), "8413245600321", null,
            3.5, 5L);
        Book book3 = createBookEntity("test book3", "test author3", "description3",
            "test publisher", LocalDate.of(2024, 4, 4), "3567134565431", null,
            4.0, 7L);

        PopularBook pb1 = PopularBook.builder()
            .book(book1)
            .period(PeriodType.DAILY)
            .rank(1L)
            .score(4.4)
            .reviewCount(5L)
            .rating(4.0)
            .build();

        PopularBook pb2 = PopularBook.builder()
            .book(book2)
            .period(PeriodType.DAILY)
            .rank(3L)
            .score(3.6)
            .reviewCount(2L)
            .rating(4.0)
            .build();

        PopularBook pb3 = PopularBook.builder()
            .book(book3)
            .period(PeriodType.DAILY)
            .rank(2L)
            .score(4.0)
            .reviewCount(4L)
            .rating(4.0)
            .build();

        popularBookRepository.saveAll(List.of(pb1, pb2, pb3));
    }

    @Test
    void 인기_도서_랭킹_조회_테스트() {

        // given
        PopularBookGetRequest request = PopularBookGetRequest.builder()
            .period(PeriodType.DAILY)
            .direction("ASC")
            .limit(10)
            .build();

        // when
        CursorPageResponse<PopularBookDto> result = popularBookService.getPopularBooks(request);

        // then
        assertFalse(result.content().isEmpty());
        assertEquals("test book1", result.content().get(0).title());
    }

    @Test
    void 잘못된_정렬_방향으로_인기_도서_순위를_요청하면_400_에러를_반환한다() {

        // given
        PopularBookGetRequest request = PopularBookGetRequest.builder()
            .period(PeriodType.DAILY)
            .direction("AESC")
            .limit(10)
            .build();

        // when
        Throwable thrown = catchThrowable(() -> popularBookService.getPopularBooks(request));

        // then
        assertThat(thrown)
            .isInstanceOf(InvalidSortDirectionException.class);
    }
}