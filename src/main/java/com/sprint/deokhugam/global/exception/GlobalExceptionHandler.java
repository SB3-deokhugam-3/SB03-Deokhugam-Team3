package com.sprint.deokhugam.global.exception;

import com.sprint.deokhugam.global.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러 도메인별 예외를 적절한 HTTP 상태코드와 메시지로 변환하여 응답
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===== 1. 비즈니스 예외 처리 =====

    /**
     * 리소스 Not Found 예외 처리 (404)
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        log.warn("[NOT_FOUND] 리소스를 찾을 수 없습니다: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getDetails(),
            "DomainException",
            HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 잘못된 요청 예외 처리 (400)
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        log.warn("[BAD_REQUEST] 잘못된 요청: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getDetails(),
            "DomainException",
            HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 인증 예외 처리 (401)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("[UNAUTHORIZED] 인증 오류: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getDetails(),
            "DomainException",
            HttpStatus.UNAUTHORIZED.value()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 인가 예외 처리 (403)
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex) {
        log.warn("[FORBIDDEN] 인가 오류: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getDetails(),
            "DomainException",
            HttpStatus.FORBIDDEN.value()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * 충돌 예외 처리 (409)
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex) {
        log.warn("[CONFLICT] 리소스 충돌: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getDetails(),
            "DomainException",
            HttpStatus.CONFLICT.value()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    // ===== 2. 유효성 검사 예외 처리 =====

    /**
     * @Valid 유효성 검사 실패 처리 (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex) {
        log.warn("[VALIDATION_FAILED] 유효성 검사 실패: {}", ex.getMessage());

        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            "INVALID_INPUT_VALUE",
            "잘못된 입력값입니다.",
            details,
            "ValidationException",
            HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // ===== 3. 일반 예외 처리 =====

    /**
     * IllegalArgumentException 처리 (400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex) {
        log.warn("[ILLEGAL_ARGUMENT] 잘못된 인수: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            "INVALID_ARGUMENT",
            ex.getMessage(),
            Map.of(),
            "IllegalArgumentException",
            HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 예상치 못한 서버 오류 처리 (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("[INTERNAL_SERVER_ERROR] 예상치 못한 오류 발생: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            "INTERNAL_SERVER_ERROR",
            "서버 내부 오류가 발생했습니다.",
            Map.of("originalMessage", ex.getMessage() != null ? ex.getMessage() : "Unknown error"),
            ex.getClass().getSimpleName(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}