package com.sprint.deokhugam.domain.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

import com.sprint.deokhugam.domain.api.dto.NaverBookDto;
import com.sprint.deokhugam.domain.book.exception.BookInfoNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class NaverBookInfoProviderTest {

    @InjectMocks
    private NaverBookInfoProvider provider;

    @Mock
    private WebClient naverApiClient;

    @Test
    void isbn으로_도서_정보를_불러올_수_있다() {

        // given
        NaverBookDto naverBookDto = NaverBookDto.builder()
            .title("아유 하기 싫어")
            .author("박명수")
            .description("하기 싫다ㅏㅏㅏ")
            .publisher("무한도전")
            .publishedDate(LocalDate.of(1999, 7, 2))
            .isbn("1999070212345")
            .thumbnailImage(Arrays.toString(new byte[]{1, 2, 3}))
            .build();

        given(provider.fetchInfoByIsbn("1999070212345")).willReturn(naverBookDto);

        // when
        NaverBookDto result = provider.fetchInfoByIsbn("1999070212345");

        // then
        assertNotNull(result);
        assertEquals("아유 하기 싫어", result.title());
        assertEquals("박명수", result.author());
        assertEquals("하기 싫다ㅏㅏㅏ", result.description());
        assertEquals("무한도전", result.publisher());
        assertEquals(LocalDate.of(1999, 7, 2), result.publishedDate());
    }

    @Test
    void isbn에_해당하는_도서_정보가_없으면_정보_불러오기에_실패한다() {

        // given
        String noInfoIsbn = "1999070212345";

        given(provider.fetchInfoByIsbn(noInfoIsbn)).willThrow(new BookInfoNotFoundException(noInfoIsbn));

        // when
        Throwable thrown = catchThrowable(() -> provider.fetchInfoByIsbn(noInfoIsbn));

        // then
        assertThat(thrown)
            .isInstanceOf(BookInfoNotFoundException.class);
    }
}