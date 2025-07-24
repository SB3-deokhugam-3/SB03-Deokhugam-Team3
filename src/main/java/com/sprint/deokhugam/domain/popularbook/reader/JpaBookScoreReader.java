package com.sprint.deokhugam.domain.popularbook.reader;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.popularbook.dto.data.BookScoreDto;
import com.sprint.deokhugam.domain.review.entity.QReview;
import com.sprint.deokhugam.global.enums.PeriodType;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public class JpaBookScoreReader {

    private final EntityManager em;
    private final PeriodType period;
    private final Instant today;

    public JpaBookScoreReader(EntityManager em, PeriodType period, Instant today) {
        this.em = em;
        this.period = period;
        this.today = today;
    }

    public List<BookScoreDto> readAll() {
        Instant start = period.getStartInstant(today, ZoneId.of("Asia/Seoul"));
        Instant end = period.getEndInstant(today, ZoneId.of("Asia/Seoul"));

        QReview r = QReview.review;

        return new JPAQueryFactory(em)
            .select(Projections.constructor(BookScoreDto.class,
                r.book.id,
                r.book.title,
                r.book.author,
                r.book.thumbnailUrl,
                r.count(),
                r.rating.avg()
            ))
            .from(r)
            .where(
                r.createdAt.goe(start),
                r.createdAt.lt(end),
                r.isDeleted.isFalse()
            )
            .groupBy(r.book.id, r.book.title, r.book.author, r.book.thumbnailUrl)
            .orderBy(
                r.count().desc(),
                r.rating.avg().desc()
            )
            .fetch();
    }
}
