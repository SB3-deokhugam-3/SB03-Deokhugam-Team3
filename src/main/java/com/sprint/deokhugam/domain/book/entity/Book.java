package com.sprint.deokhugam.domain.book.entity;

import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.global.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
@Entity
@Table(name = "books")
public class Book extends BaseUpdatableEntity  {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String publisher;

    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    @Column
    private String isbn;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "review_count", nullable = false)
    private Long reviewCount = 0L;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();


    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateAuthor(String author) {
        this.author = author;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updatePublisher(String publisher) {
        this.publisher = publisher;
    }

    public void updatePublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void updateThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void updateRating(Double newRating) {
        this.rating = newRating;
    }

    public void increaseReviewCount() {
        this.reviewCount++;
    }

    public void decreaseReviewCount() {
        if (this.reviewCount > 0) {
            this.reviewCount--;
        }
    }

    public void delete() {
        this.isDeleted = true;
    }

    public boolean isDeleted() {
        return this.isDeleted;
    }

    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }
}
