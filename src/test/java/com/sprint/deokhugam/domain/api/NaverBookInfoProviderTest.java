package com.sprint.deokhugam.domain.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NaverBookInfoProviderTest {

    @InjectMocks
    private NaverBookInfoProvider provider;

    @Test
    void isbn으로_도서_정보를_불러올_수_있다() {

        // given
        NaverBookDto naverBookDto = NaverBookDto.builder()
            .title("아유 하기 싫어")
            .author("박명수")
            .descritption("하기 싫다ㅏㅏㅏ")
            .publisher("무한도전")
            .publishedDate(LocalDate.of(1999, 7, 2))
            .isbn("1999070212345")
            .thumbnailImage(new byte[]{1, 2, 3})
            .build();

        given(provider.fetchInfoByIsbn(isbn)).willReturn(naverBookDto);

        // when
        NaverBookDto result = provider.fetchInfoByIsbn(isbn);

        // then
        assertNotNull(result);
        assertEquals("아유 하기 싫어", result.title());
        assertEquals("박명수", result.author());
        assertEquals("하기 싫다ㅏㅏㅏ", result.description());
        assertEquals("무한도전", result.publisher());
        assertEquals(LocalDate.of(1999, 7, 2), result.publishedDate());
        assertEquals(3, result.thumbnailImage.length);
    }

    @Test
    void isbn에_해당하는_도서_정보가_없으면_정보_불러오기에_실패한다() {

        // given
        String noInfoIsbn = "1999070212345";

        given(provider.fetchInfoByIsbn(noInfoIsbn)).willThrow(new BookInfoNowFoundException("BOOK INFO",
            Map.of("isbn", isbn)));

        // when
        Throwable thrown = catchThrowable(() -> provider.fetchInfoByIsbn(noInfoIsbn));

        // then
        assertThat(thrown)
            .isInstanceOf(BookInfoNotFoundException.class);
    }
}