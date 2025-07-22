package com.sprint.deokhugam.domain.popularbook.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sprint.deokhugam.domain.book.storage.s3.S3Storage;
import com.sprint.deokhugam.domain.popularbook.dto.data.PopularBookDto;
import com.sprint.deokhugam.domain.popularbook.dto.request.PopularBookGetRequest;
import com.sprint.deokhugam.domain.popularbook.exception.InvalidSortDirectionException;
import com.sprint.deokhugam.domain.popularbook.repository.PopularBookRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.period.PeriodType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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

    @Mock
    private S3Storage s3Storage;

    private PopularBookDto dto1;
    private PopularBookDto dto2;
    private PopularBookDto dto3;

    @BeforeEach
    void setUp() {
        UUID book1Id = UUID.randomUUID();
        UUID book2Id = UUID.randomUUID();
        UUID book3Id = UUID.randomUUID();
        UUID pb1Id = UUID.randomUUID();
        UUID pb2Id = UUID.randomUUID();
        UUID pb3Id = UUID.randomUUID();

        dto1 = new PopularBookDto(pb1Id, book1Id, "test book1", "test author1",
            "test.jpg", PeriodType.DAILY, 1L,
            4.4, 5L, 4.0, Instant.now());
        dto2 = new PopularBookDto(pb2Id, book2Id, "test book2", "test author2", null,
            PeriodType.DAILY, 3L, 3.6, 2L, 4.0, Instant.now());
        dto3 = new PopularBookDto(pb3Id, book3Id, "test book3", "test author3", null,
            PeriodType.DAILY, 2L, 4.0, 4L, 4.0, Instant.now());
    }

    @Test
    void 인기_도서_랭킹_조회_테스트() {

        // given
        PopularBookGetRequest request = PopularBookGetRequest.builder()
            .period(PeriodType.DAILY)
            .direction("ASC")
            .limit(10)
            .build();

        given(popularBookRepository.findAllByRequest(request)).willReturn(List.of(dto1, dto2, dto3));
        given(popularBookRepository.countByPeriod(PeriodType.DAILY)).willReturn(3L);
        given(s3Storage.generatePresignedUrl("test.jpg")).willReturn("https://cdn.example.com/cover.png");

        // when
        CursorPageResponse<PopularBookDto> result = popularBookService.getPopularBooks(request);

        // then
        assertFalse(result.content().isEmpty());
        assertEquals("test book1", result.content().get(0).getTitle());
        verify(s3Storage).generatePresignedUrl(any());
    }

    @Test
    void 커서_기반_인기_도서_랭킹_조회_다음_페이지_조회() {

        // given
        PopularBookGetRequest request = PopularBookGetRequest.builder()
            .period(PeriodType.DAILY)
            .direction("ASC")
            .limit(2)
            .build();

        given(popularBookRepository.findAllByRequest(request)).willReturn(List.of(dto1, dto3, dto2));
        given(popularBookRepository.countByPeriod(PeriodType.DAILY)).willReturn(3L);
        given(s3Storage.generatePresignedUrl("test.jpg")).willReturn("https://cdn.example.com/cover.png");

        // when
        CursorPageResponse<PopularBookDto> result = popularBookService.getPopularBooks(request);

        // then
        assertFalse(result.content().isEmpty());
        assertEquals("test book1", result.content().get(0).getTitle());
        assertTrue(result.hasNext());
        assertEquals("2", result.nextCursor());
        assertNotNull(result.nextAfter());
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