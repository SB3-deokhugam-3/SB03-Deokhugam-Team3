package com.sprint.deokhugam.domain.popularbook.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.book.entity.QBook;
import com.sprint.deokhugam.domain.popularbook.dto.data.PopularBookDto;
import com.sprint.deokhugam.domain.popularbook.dto.request.PopularBookGetRequest;
import com.sprint.deokhugam.domain.popularbook.entity.QPopularBook;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PopularBookRepositoryCustomImpl implements PopularBookRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QPopularBook pb = QPopularBook.popularBook;
    private static final QBook b = QBook.book;

    @Override
    public List<PopularBookDto> findAllByRequest(PopularBookGetRequest request) {
        PeriodType period = request.period();
        String direction = request.direction();
        String cursor = request.cursor();
        String after = request.after();
        int size = request.limit();

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(pb.period.eq(period));

        if (cursor != null && after != null) {
            try {
                Long cursorRank = Long.parseLong(cursor);
                Instant afterTime = Instant.parse(after);
                if (direction.equalsIgnoreCase("asc")) {
                    builder.and(pb.rank.gt(cursorRank)
                        .or(pb.rank.eq(cursorRank).and(pb.createdAt.gt(afterTime)))
                    );
                } else {
                    builder.and(pb.rank.lt(cursorRank)
                        .or(pb.rank.eq(cursorRank).and(pb.createdAt.lt(afterTime)))
                    );
                }
            } catch (NumberFormatException e) {
                log.warn("[PopularBookRepository] Invalid cursor format: {} ", cursor);
                throw e;
            } catch (DateTimeParseException e) {
                log.warn("[PopularBookRepository] Invalid after format: {}", after);
                throw e;
            }
        }

        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(direction);

        return queryFactory
            .select(Projections.constructor(PopularBookDto.class,
                pb.id,
                pb.book.id,
                pb.book.title,
                pb.book.author,
                pb.book.thumbnailUrl,
                pb.period,
                pb.rank,
                pb.score,
                pb.reviewCount,
                pb.rating,
                pb.createdAt))
            .from(pb)
            .join(pb.book, b)
            .where(builder)
            .orderBy(orderSpecifier)
            .limit(size + 1)
            .fetch();
    }

    @Override
    public long deleteByPeriod(PeriodType period) {
        return queryFactory
            .delete(pb)
            .where(pb.period.eq(period))
            .execute();
    }

    private OrderSpecifier<?> getOrderSpecifier(String direction) {
        if (direction.equalsIgnoreCase("asc")) {
            return pb.rank.asc();
        } else {
            return pb.rank.desc();
        }
    }
}
