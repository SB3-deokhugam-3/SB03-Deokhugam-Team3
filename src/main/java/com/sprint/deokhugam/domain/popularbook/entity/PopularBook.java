package com.sprint.deokhugam.domain.popularbook.entity;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.global.base.BaseEntity;
import com.sprint.deokhugam.global.period.PeriodType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "popular_book_rankings")
@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularBook extends BaseEntity {

    // ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')
    @Column(name = "period", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private PeriodType period = PeriodType.DAILY;

    @Column(name = "rank", nullable = false)
    private Long rank = 0L;

    // 점수 = (해당 기간의 리뷰 수 * 0.4) + (해당 기간의 평점 평균 * 0.6)
    @Column(name = "score", nullable = false)
    private Double score = 0.0;

    @Column(name = "review_count", nullable = false)
    private Long reviewCount = 0L;

    @Column(name = "rating", nullable = false)
    private Double rating = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    public void updateRank(Long rank) {
        this.rank = rank;
    }
}
