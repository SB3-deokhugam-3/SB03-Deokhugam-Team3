package com.sprint.deokhugam.domain.reviewlike.mapper;

import com.sprint.deokhugam.domain.reviewlike.dto.data.ReviewLikeDto;
import com.sprint.deokhugam.domain.reviewlike.entity.ReviewLike;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewLikeMapper {

    @Mapping(target = "reviewId", source = "review.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "liked", constant = "true")
    ReviewLikeDto toDto(ReviewLike reviewLike);
}
