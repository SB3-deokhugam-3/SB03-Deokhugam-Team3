package com.sprint.deokhugam.global.inteceptor;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MDCLoggingInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID = "request-id";
    private static final String REQUEST_METHOD = "http-method";
    private static final String REQUEST_URL = "request-uri";
    private static final String IP_ADDRESS = "ip";
    private static final String HEADER_NAME = "Deokhugam-Request-id";
    private static final String HEADER_IP = "Deokhugam-Client-id";


    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler) {

        String requestId = UUID.randomUUID().toString().substring(0, 12);
        String method = request.getMethod();
        String url = request.getRequestURI();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        MDC.put(REQUEST_ID, requestId);
        MDC.put(REQUEST_METHOD, method);
        MDC.put(REQUEST_URL, url);
        MDC.put(IP_ADDRESS, ip);

        response.setHeader(HEADER_NAME, requestId);
        response.setHeader(HEADER_IP, ip);

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler, @Nullable Exception ex) {
        MDC.clear();
    }
}
