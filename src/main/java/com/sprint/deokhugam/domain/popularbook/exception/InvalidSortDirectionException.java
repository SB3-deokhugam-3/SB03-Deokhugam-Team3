package com.sprint.deokhugam.domain.popularbook.exception;

import com.sprint.deokhugam.global.exception.InvalidTypeException;
import java.util.Map;

public class InvalidSortDirectionException extends InvalidTypeException {

    public InvalidSortDirectionException(String sortDirection) {
        super(Map.of("sortDirection", sortDirection,
            "message", "잘못된 정렬 방향입니다."));
    }
}
