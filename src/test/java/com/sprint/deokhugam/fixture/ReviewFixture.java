package com.sprint.deokhugam.fixture;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.global.enums.PeriodType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ReviewFixture {
    public static User user(String email, String nickname) {
        return User.builder()
            .email(email)
            .nickname(nickname)
            .password("pw")
            .build();
    }

    public static Book book(String title, String isbn) {
        return Book.builder()
            .title(title)
            .author("작가")
            .publisher("출판사")
            .description("설명")
            .isbn(isbn)
            .publishedDate(LocalDate.now())
            .thumbnailUrl("http://example.com/" + title + ".jpg")
            .rating(4.0)
            .reviewCount(0L)
            .isDeleted(false)
            .build();
    }

    public static Review review(User user, Book book, String content, long like, long comment, boolean deleted) {
        return Review.builder()
            .user(user)
            .book(book)
            .content(content)
            .rating(5)
            .likeCount(like)
            .commentCount(comment)
            .isDeleted(deleted)
            .build();
    }

    public static PopularReview popularReview(Review review, PeriodType period, long rank, double score) {
        return PopularReview.builder()
            .review(review)
            .period(period)
            .rank(rank)
            .score(score)
            .likeCount(review.getLikeCount())
            .commentCount(review.getCommentCount())
            .build();
    }

    public static PopularReviewDto dto(PopularReview popularReview) {
        Review review = popularReview.getReview();
        Book book = review.getBook();
        User user = review.getUser();
        long rank = popularReview.getRank();
        double score = popularReview.getScore();
        PeriodType period = popularReview.getPeriod();
        UUID id = UUID.randomUUID();

        return new PopularReviewDto(
            id,
            review.getId(),
            book.getId(),
            book.getTitle(),
            "https://s3.example.com/converted.jpg",
            user.getId(),
            user.getNickname(),
            review.getContent(),
            (double) review.getRating(),
            period,
            Instant.now(),
            rank,
            score,
            review.getLikeCount(),
            review.getCommentCount()
        );
    }

}
