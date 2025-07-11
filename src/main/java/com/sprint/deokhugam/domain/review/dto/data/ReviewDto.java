package com.sprint.deokhugam.domain.review.dto.data;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewDto {

    private UUID id;
    private UUID bookId;
    private String bookTitle;
    private String bookThumbnailUrl;
    private UUID userId;
    private String userNickname;
    private String content;
    private Double rating;
    private Long likeCount;
    private Long commentCount;
    private Boolean likedByMe;
    private Instant createdAt;
    private Instant updatedAt;

}
