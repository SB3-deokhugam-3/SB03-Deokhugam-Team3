package com.sprint.deokhugam.domain.comment.entity;

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
import org.hibernate.annotations.Where;

@Entity
@Getter
@Table(name = "comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "is_deleted = false")
public class Comment extends BaseUpdatableEntity {
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "review_id", columnDefinition = "uuid", nullable = false)
//    private Review review;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", columnDefinition = "uuid", nullable = false)
//    private User user;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

//    public Comment(Review review, User user, String content) {
//        this.review = review;
//        this.user = user;
//        this.content = content;
//        this.isDeleted = false;
//    }

    public void update(String newContent) {
        if (newContent != null && !newContent.equals(this.content)) {
            this.content = newContent;
        }
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}
