package com.sprint.deokhugam.domain.review.mapper;

import com.sprint.deokhugam.global.storage.S3Storage;
import com.sprint.deokhugam.domain.review.dto.data.ReviewDto;
import com.sprint.deokhugam.domain.review.entity.Review;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "bookThumbnailUrl", expression = "java(generateThumbnailUrl(review.getBook().getThumbnailUrl(), s3Storage))")
    @Mapping(target = "userNickname", source = "user.nickname")
    @Mapping(target = "likedByMe", ignore = true)
    ReviewDto toDto(Review review, @Context S3Storage s3Storage);

    default String generateThumbnailUrl(String thumbnailKey, S3Storage s3Storage) {
        if (thumbnailKey != null && !thumbnailKey.isBlank()) {
            return s3Storage.generatePresignedUrl(thumbnailKey);
        }
        return null;
    }
}
