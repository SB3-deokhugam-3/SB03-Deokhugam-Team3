package com.sprint.deokhugam.global.config;

import com.sprint.deokhugam.global.interceptor.LoginInterceptor;
import com.sprint.deokhugam.global.interceptor.MDCLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final MDCLoggingInterceptor mdcLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mdcLoggingInterceptor)
            .addPathPatterns("/api/**");

        // 로그인, 회원가입, 대시보드 관련 api만 허용
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                    .excludePathPatterns("/api/users/login", "/api/users",
                        "/api/users/power", "/api/books/popular", "/api/reviews/popular");
    }
}
