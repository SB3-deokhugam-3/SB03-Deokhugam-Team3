package com.sprint.deokhugam.domain.book.mapper;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.user.storage.s3.S3Storage;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {

    // BookCreateRequest -> Book
    @Mapping(target = "rating", expression = "java(0.0)")
    @Mapping(target = "reviewCount", expression = "java(0L)")
    @Mapping(target = "isDeleted", expression = "java(false)")
    Book toEntity(BookCreateRequest request);

    default BookDto toDto(Book book, @Context S3Storage s3Storage) {
        if (book == null) {
            return null;
        }

        String thumbnailUrl = null;
        if (book.getThumbnailUrl() != null && !book.getThumbnailUrl().isEmpty()) {
            thumbnailUrl = s3Storage.generatePresignedUrl(book.getThumbnailUrl());
        }

        return new BookDto(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getDescription(),
            book.getPublisher(),
            book.getPublishedDate(),
            book.getIsbn(),
            thumbnailUrl,
            book.getReviewCount(),
            book.getRating(),
            book.getCreatedAt(),
            book.getUpdatedAt()
        );
    }
}
