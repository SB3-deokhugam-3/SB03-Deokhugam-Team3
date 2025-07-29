package com.sprint.deokhugam.domain.popularreview.entity;

import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.global.base.BaseEntity;
import com.sprint.deokhugam.global.enums.PeriodType;
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
import lombok.ToString;

@Entity
@Table(name = "popular_review_rankings")
@AllArgsConstructor
@Builder
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularReview extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "period", length = 10, nullable = false)
    private PeriodType period;

    @Column(name = "rank", nullable = false)
    private Long rank = 0L;

    //점수 = (해당 기간의 좋아요 수 * 0.3) + (해당 기간의 댓글 수 * 0.7)
    @Column(name = "score", nullable = false)
    private Double score = 0.0;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;
}
