package com.sprint.deokhugam.domain.book.mapper;

import com.sprint.deokhugam.domain.book.dto.data.BookDto;
import com.sprint.deokhugam.domain.book.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookDto toBookDto(Book book) {
        return new BookDto(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getDescription(),
            book.getPublisher(),
            book.getPublishedDate(),
            book.getIsbn(),
            book.getThumbnailUrl(),
            book.getReviewCount(),
            book.getRating(),
            book.getCreatedAt(),
            book.getUpdatedAt()
        );
    }
}
