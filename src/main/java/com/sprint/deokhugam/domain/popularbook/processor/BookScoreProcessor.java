package com.sprint.deokhugam.domain.popularbook.processor;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.popularbook.dto.data.BookScoreDto;
import com.sprint.deokhugam.domain.popularbook.entity.PopularBook;
import com.sprint.deokhugam.global.enums.PeriodType;
import jakarta.persistence.EntityManager;
import org.springframework.batch.item.ItemProcessor;

public class BookScoreProcessor implements ItemProcessor<BookScoreDto, PopularBook> {

    private final EntityManager em;
    private final PeriodType period;

    public BookScoreProcessor(EntityManager em, PeriodType period) {
        this.em = em;
        this.period = period;
    }

    @Override
    public PopularBook process(BookScoreDto dto) {
        if (dto.bookId() == null) {
            return null;
        }

        Book book = em.find(Book.class, dto.bookId());

        return PopularBook.builder()
            .period(period)
            .score(dto.calculateScore())
            .reviewCount(dto.reviewCount())
            .rating(dto.rating())
            .book(book)
            .build();
    }
}
