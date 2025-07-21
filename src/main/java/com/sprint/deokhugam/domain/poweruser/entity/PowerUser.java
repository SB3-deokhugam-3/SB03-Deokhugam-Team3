package com.sprint.deokhugam.domain.poweruser.entity;

import com.sprint.deokhugam.domain.user.entity.User;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Table(name = "power_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PowerUser extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false)
    private PeriodType period;

    @Column(name = "rank", nullable = false)
    private Long rank;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "review_score_sum", nullable = false)
    private Double reviewScoreSum;

    @Column(name = "like_count", nullable = false)
    private Long likeCount;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount;

    // QueryDSL Projections.constructor를 위한 생성자 추가
    public PowerUser(User user, PeriodType period, Long rank, Double score,
        Double reviewScoreSum, Long likeCount, Long commentCount) {
        if (user == null || period == null) {
            throw new IllegalArgumentException("user와 period는 필수입니다.");
        }
        if (rank == null || rank < 1) {
            throw new IllegalArgumentException("순위는 1 이상이어야 합니다.");
        }
        if (score == null || score < 0) {
            throw new IllegalArgumentException("점수는 0 이상이어야 합니다.");
        }
        this.user = user;
        this.period = period;
        this.rank = rank;
        this.score = score;
        this.reviewScoreSum = reviewScoreSum;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    public void updateRank(Long newRank) {
        if (newRank == null || newRank < 1) {
            throw new IllegalArgumentException("순위는 1 이상이어야 합니다.");
        }
        this.rank = newRank;
    }
}
