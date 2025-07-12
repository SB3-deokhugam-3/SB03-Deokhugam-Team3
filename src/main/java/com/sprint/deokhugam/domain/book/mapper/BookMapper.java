package com.sprint.deokhugam.domain.book.mapper;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.dto.request.BookCreateRequest;
import com.sprint.deokhugam.domain.book.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {

    // BookCreateRequest -> Book
    @Mapping(target = "rating", expression = "java(0.0)")
    @Mapping(target = "reviewCount", expression = "java(0L)")
    @Mapping(target = "isDeleted", expression = "java(false)")
    Book toEntity(BookCreateRequest request);

    // Book -> BookDto
    BookDto toDto(Book book);
}
