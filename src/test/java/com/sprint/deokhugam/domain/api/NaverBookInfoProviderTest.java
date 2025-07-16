package com.sprint.deokhugam.domain.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

import com.sprint.deokhugam.domain.api.dto.NaverBookDto;
import com.sprint.deokhugam.domain.book.exception.BookInfoNotFoundException;
import com.sprint.deokhugam.domain.book.exception.InvalidIsbnException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class NaverBookInfoProviderTest {

    @Spy
    @InjectMocks
    private NaverBookInfoProvider provider;

    @Mock
    private WebClient naverApiClient;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @SuppressWarnings("unchecked")
    @Test
    void isbn으로_도서_정보를_불러올_수_있다() {

        // given
        String jsonResponse = """
        {
          "items": [
            {
              "title": "아유 하기 싫어",
              "author": "박명수",
              "description": "하기 싫다ㅏㅏㅏ",
              "publisher": "무한도전",
              "pubdate": "19990702",
              "isbn": "1999070212345",
              "image": "http://dummyimage.com/100.jpg"
            }
          ]
        }
        """;

        given(naverApiClient.get()).willReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        given(requestHeadersUriSpec.uri(any(Function.class))).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(jsonResponse));
        doReturn("testImage").when(provider).imageToBase64(anyString());

        // when
        NaverBookDto result = provider.fetchInfoByIsbn("1999070212345");

        // then
        assertNotNull(result);
        assertEquals("아유 하기 싫어", result.title());
        assertEquals("박명수", result.author());
        assertEquals("하기 싫다ㅏㅏㅏ", result.description());
        assertEquals("무한도전", result.publisher());
        assertEquals(LocalDate.of(1999, 7, 2), result.publishedDate());
        assertEquals("testImage", result.thumbnailImage());
    }

    @Test
    void isbn에_해당하는_도서_정보가_없으면_정보_불러오기에_실패한다() {

        // given
        String noInfoIsbn = "1999070212345";
        String jsonResponse = """
        {
          "items": []
        }
        """;

        given(naverApiClient.get()).willReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        given(requestHeadersUriSpec.uri(any(Function.class))).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(jsonResponse));

        // when
        Throwable thrown = catchThrowable(() -> provider.fetchInfoByIsbn(noInfoIsbn));

        // then
        assertThat(thrown)
            .isInstanceOf(BookInfoNotFoundException.class);
    }

    @Test
    void 잘못된_isbn_형식으로_도서_정보_조회_요청을_하면_실패한다() {

        // given
        String wrongIsbn = "1111222233334444";

        // when
        Throwable thrown = catchThrowable(() -> provider.fetchInfoByIsbn(wrongIsbn));

        // then
        assertThat(thrown)
            .isInstanceOf(InvalidIsbnException.class);
    }
}