package com.sprint.deokhugam.domain.review.entity;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("is_deleted <> false")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Review(Double rating, String content, Book book, User user) {
        this.rating = rating;
        this.content = content;
        this.book = book;
        this.user = user;
    }

    public void update(String newContent, Double newRating) {
        if (newContent != null && !newContent.trim().isEmpty()) {
            this.content = newContent;
        }
        if (newRating != null && newRating >= 0.0 && newRating <= 5.0) {
            this.rating = newRating;
        }
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void softDelete() {
        this.isDeleted = true;
    }

}
