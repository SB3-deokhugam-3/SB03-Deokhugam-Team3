package com.sprint.deokhugam.domain.popularreview.mapper;

import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.global.storage.S3Storage;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PopularReviewMapper {

    @Mapping(target = "reviewId", source = "review.id")
    @Mapping(target = "bookId", source = "review.book.id")
    @Mapping(target = "bookTitle", source = "review.book.title")
    @Mapping(target = "bookThumbnailUrl", expression = "java(generateThumbnailUrl(popularReview.getReview().getBook().getThumbnailUrl(), s3Storage))")
    @Mapping(target = "userId", source = "review.user.id")
    @Mapping(target = "userNickname", source = "review.user.nickname")
    @Mapping(target = "reviewContent", source = "review.content")
    @Mapping(target = "reviewRating", source = "review.rating")
    PopularReviewDto toDto(PopularReview popularReview, @Context S3Storage s3Storage);

    default String generateThumbnailUrl(String thumbnailKey, S3Storage s3Storage) {
        if (thumbnailKey != null && !thumbnailKey.isBlank()) {
            return s3Storage.generatePresignedUrl(thumbnailKey);
        }
        return null;
    }
}
