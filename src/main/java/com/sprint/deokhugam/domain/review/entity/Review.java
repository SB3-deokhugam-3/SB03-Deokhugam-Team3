package com.sprint.deokhugam.domain.review.entity;

import com.sprint.deokhugam.global.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
public class Review extends BaseUpdatableEntity {

    @Column(name = "rating", nullable = false)
    private Double rating = 0.0;

    @Column(name = "content", nullable = false)
    private String content = "";

    @Column(name = "like_count")
    private Long likeCount = 0L;

    @Column(name = "comment_count")
    private Long commentCount = 0L;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

//    @ManyToOne
//    @JoinColumn(name = "book_id")
//    private Book book;

//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;

}
