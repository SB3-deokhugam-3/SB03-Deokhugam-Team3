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
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        IllegalArgumentException.class,
        MissingRequestHeaderException.class,
        MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(Exception ex) {
        Map<String, Object> details = new HashMap<>();
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        String logMessage;

        if (ex instanceof MethodArgumentNotValidException validationEx) {
            logMessage = "[VALIDATION_FAILED] 유효성 검사 실패: " + validationEx.getMessage();

            validationEx.getBindingResult().getAllErrors().forEach(error -> {
                if (error instanceof FieldError fieldError) {
                    details.put(fieldError.getField(), fieldError.getDefaultMessage());
                } else {
                    details.put(error.getObjectName(), error.getDefaultMessage());
                }
            });
            errorCode = ErrorCode.INVALID_INPUT_VALUE;

        } else if (ex instanceof MissingRequestHeaderException headerEx) {
            logMessage = "[MISSING_REQUEST_HEADER] 필수 헤더 누락: " + headerEx.getHeaderName();

            if (debugEnabled) {
                details.put("missingHeader", headerEx.getHeaderName());
            }
            errorCode = ErrorCode.MISSING_REQUEST_HEADER;

        } else if (ex instanceof MissingServletRequestParameterException paramEx) {
            logMessage = "[MISSING_REQUEST_PARAMETER] 필수 파라미터 누락: " + paramEx.getParameterName();

            if (debugEnabled) {
                details.put("missingParameter", paramEx.getParameterName());
                details.put("parameterType", paramEx.getParameterType());
            }
            errorCode = ErrorCode.MISSING_REQUEST_PARAMETER;

        } else {
            logMessage = "[ILLEGAL_ARGUMENT] 잘못된 요청: " + ex.getMessage();

            if (debugEnabled) {
                details.put("originalMessage", ex.getMessage());
            }
            errorCode = ErrorCode.INVALID_INPUT_VALUE;
        }

        log.warn(logMessage);

        DomainException badRequestException = new DomainException(errorCode, details);
        return toErrorResponse(badRequestException);
    }

    /**
     * NoResourceFoundException 처리 (404) - 요청한 경로가 없을때
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
        NoResourceFoundException ex) {
        log.warn("[RESOURCE_NOT_FOUND] 요청한 경로 없음 code={}, message={}",
            ErrorCode.NO_RESOURCE_FOUND.getCode(),
            ErrorCode.NO_RESOURCE_FOUND.getMessage());

        Map<String, Object> details = debugEnabled
            ? Map.of("originalMessage", ex.getMessage())
            : Map.of();

        DomainException argumentException = new DomainException(ErrorCode.NO_RESOURCE_FOUND,
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