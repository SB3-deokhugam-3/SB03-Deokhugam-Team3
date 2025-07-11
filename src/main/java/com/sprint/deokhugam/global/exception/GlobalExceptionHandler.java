package com.sprint.deokhugam.global.exception;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends RuntimeException {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", ex.getMessage()));
    }

//  @ExceptionHandler(CommonException.class)
//  public ResponseEntity<ErrorResponse> handleCommonException(CommonException e) {
//    // 도메인 별 공통 로깅
//    log.error("▶▶▶▶Exception(공통) 발생 : {} - {}", e.getErrorCode().getCode(), e.getMessage());
//
//    // 컨텍스트 정보 로깅
//    e.getDetails().forEach((key, value) ->
//        log.debug("▶▶▶▶▶Exception(공통) 세부정보 : {} = {}", key, value));
//
//    return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(
//        new ErrorResponse(
//            e.getTimestamp(), e.getErrorCode().getCode(),
//            e.getMessage(), e.getDetails(),
//            e.getClass().getTypeName(),
//            e.getErrorCode().getHttpStatus()
//        )
//    );
//  }

}
