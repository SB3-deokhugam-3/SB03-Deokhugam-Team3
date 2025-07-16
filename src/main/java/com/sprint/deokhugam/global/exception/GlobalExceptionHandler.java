package com.sprint.deokhugam.global.exception;

import com.sprint.deokhugam.global.dto.response.ErrorResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * 전역 예외 처리 핸들러 도메인별 예외를 적절한 HTTP 상태코드와 메시지로 변환하여 응답
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${app.debug.enabled:false}")
    private boolean debugEnabled;

    /**
     * 도메인 예외 공통 처리 메서드
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDeokhugamException(DomainException ex) {
        log.warn("[예외 처리] DomainException 발생: {}", ex.getMessage(), ex);
        log.debug("[예외 처리] 에러 코드: {}, HTTP 상태: {}", ex.getErrorCodeString(),
            ex.getErrorCode().getStatus());

        return ResponseEntity.status(ex.getErrorCode().getHttpStatus())
            .body(ErrorResponse.of(ex));
    }

    /**
     * @Valid 유효성 검사 실패 처리 (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex) {
        log.warn("[VALIDATION_FAILED] 유효성 검사 실패: {}", ex.getMessage());

        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                details.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                details.put(error.getObjectName(), error.getDefaultMessage());
            }
        });

        DomainException validationException = new DomainException(
            ErrorCode.INVALID_INPUT_VALUE, details);

        return toErrorResponse(validationException);
    }

    /**
     * IllegalArgumentException 처리 (400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex) {
        log.warn("[ILLEGAL_ARGUMENT] code={}, message={}", ErrorCode.INVALID_INPUT_VALUE.name(),
            ex.getMessage());

        Map<String, Object> details = debugEnabled
            ? Map.of("originalMessage", ex.getMessage())
            : Map.of();

        DomainException argumentException = new DomainException(ErrorCode.INVALID_INPUT_VALUE,
            details);

        return toErrorResponse(argumentException);
    }

    /**
     * 예상치 못한 서버 오류 처리 (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("[INTERNAL_SERVER_ERROR] 예상치 못한 오류 발생: {}", ex.getMessage(), ex);

        // 디버그 모드일 때만 상세 정보 노출
        Map<String, Object> details = debugEnabled
            ? Map.of("originalMessage", ex.getMessage() != null ? ex.getMessage() : "Unknown error")
            : Map.of();

        DomainException internalException = new DomainException(
            ErrorCode.INTERNAL_SERVER_ERROR, details);

        return toErrorResponse(internalException);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingPart(MissingServletRequestPartException ex) {
        log.warn("[MISSING_REQUEST_PART] 필수 요청 파트 누락: {}", ex.getMessage());

        DomainException domainException = new DomainException(
            ErrorCode.INVALID_INPUT_VALUE,
            Map.of("message", "필수 이미지 파일이 요청에 없습니다.")
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(domainException));
    }

    private ResponseEntity<ErrorResponse> toErrorResponse(DomainException ex) {
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus())
            .body(ErrorResponse.of(ex));
    }
}