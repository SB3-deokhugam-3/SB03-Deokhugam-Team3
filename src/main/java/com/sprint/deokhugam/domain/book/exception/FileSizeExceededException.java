package com.sprint.deokhugam.domain.book.exception;

import com.sprint.deokhugam.global.exception.BadRequestException;
import java.util.Map;

public class FileSizeExceededException extends BadRequestException {

    public FileSizeExceededException(long fileSize, long maxSize) {
        super("FILE", Map.of("fileSize", fileSize, "maxSize", maxSize));
    }
}
