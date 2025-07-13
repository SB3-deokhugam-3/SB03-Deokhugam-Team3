package com.sprint.deokhugam.domain.review.mapper;

import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "bookThumbnailUrl", source = "book.thumbnailUrl")
    @Mapping(target = "userNickname", source = "user.nickname")
    @Mapping(target = "likedByMe", ignore = true)
    ReviewDto toDto(Review review);

}
