package com.sprint.deokhugam.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient naverApiClient(
        @Value("${books.api.naver.client-id}") String clientId,
        @Value("${books.api.naver.client-secret}") String clientSecret) {
        return WebClient.builder()
            .baseUrl("https://openapi.naver.com/v1/search")
            .defaultHeader("X-Naver-Client-Id", clientId)
            .defaultHeader("X-Naver-Client-Secret", clientSecret)
            .build();
    }
}
