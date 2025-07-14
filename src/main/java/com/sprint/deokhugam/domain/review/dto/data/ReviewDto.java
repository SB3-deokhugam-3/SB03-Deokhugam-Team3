package com.sprint.deokhugam.domain.review.dto.data;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record ReviewDto(
    UUID id,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl,
    UUID userId,
    String userNickname,
    String content,
    Double rating,
    Long likeCount,
    Long commentCount,
    Boolean likedByMe,
    Instant createdAt,
    Instant updatedAt

) {

}
