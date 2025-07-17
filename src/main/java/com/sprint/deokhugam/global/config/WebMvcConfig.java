package com.sprint.deokhugam.global.config;

import com.sprint.deokhugam.global.interceptor.LoginInterceptor;
import com.sprint.deokhugam.global.interceptor.MDCLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final MDCLoggingInterceptor mdcLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mdcLoggingInterceptor)
            .addPathPatterns("/api/**");

        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                    .excludePathPatterns("/api/users/login", "/api/users"); // 로그인 회원가입 요청만 허용
    }
}
