package com.sprint.deokhugam.domain.book.entity;

import com.sprint.deokhugam.global.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(nullable = false)
    private LocalDate publishedDate;

    @Column
    private String isbn;

    @Column
    private String thumbnailUrl;

    @Column(nullable = false)
    private Long reviewCount = 0L;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(nullable = false)
    private boolean isDeleted = false;

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
}
