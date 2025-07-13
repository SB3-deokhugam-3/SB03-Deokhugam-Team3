package com.sprint.deokhugam.global.dto.response;

import java.util.List;

public record CursorPageResponse<T>(
    List<T> content,
    String nextCursor,
    String nextAfter,
    int size,
    Long totalElements,
    boolean hasNext
) {

}