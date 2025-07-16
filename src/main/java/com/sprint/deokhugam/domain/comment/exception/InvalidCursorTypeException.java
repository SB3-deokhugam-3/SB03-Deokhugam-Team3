package com.sprint.deokhugam.domain.comment.exception;

import com.sprint.deokhugam.global.exception.BadRequestException;
import java.util.Map;

public class InvalidCursorTypeException extends BadRequestException {

  public InvalidCursorTypeException(String cursor, String message) {
    super("comment", Map.of(
        "cursor", cursor,
        "message", "잘못된 커서 형식입니다"
    ));
  }
}