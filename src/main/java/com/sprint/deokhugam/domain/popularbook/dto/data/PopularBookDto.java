package com.sprint.deokhugam.domain.popularbook.dto.data;

import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class PopularBookDto {

    private UUID id;
    private UUID bookId;
    private String title;
    private String author;
    private String thumbnailUrl;
    private PeriodType period;
    private Long rank;
    private Double score;
    private Long reviewCount;
    private Double rating;
    private Instant createdAt;

    public void updateThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
