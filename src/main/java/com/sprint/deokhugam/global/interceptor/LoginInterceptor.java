package com.sprint.deokhugam.global.interceptor;

import com.sprint.deokhugam.domain.user.exception.MissingUserIdHeaderException;
import com.sprint.deokhugam.global.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final String HEADER_USER_ID = "Deokhugam-Request-User-ID";
    private static final String USER_ID = "userId";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler) {

        // 사용자 ID 추출
        String userIdHeader = request.getHeader(HEADER_USER_ID);

        if (userIdHeader == null || userIdHeader.isEmpty()) {
            throw new MissingUserIdHeaderException(HEADER_USER_ID + " 헤더를 찾을 수 없습니다.");
        }

        try {
            UUID userId = UUID.fromString(userIdHeader);
            request.setAttribute(USER_ID, userId);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException(Map.of(USER_ID, userIdHeader));
        }

        return true;
    }
}
