package com.sprint.deokhugam.domain.review.repository;

import com.sprint.deokhugam.domain.book.entity.Book;
import com.sprint.deokhugam.domain.review.entity.Review;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, CustomReviewRepository {

    boolean existsByBookIdAndUserId(UUID bookId, UUID userId);

    @Query(value = "SELECT * FROM reviews WHERE id = :id AND is_deleted = true", nativeQuery = true)
    Optional<Review> findDeletedById(@Param("id") UUID reviewId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book = :book")
    Double findAverageRatingByBook(@Param("book") Book book);

    List<Review> findAllByIdInAndIsDeletedFalse(Collection<UUID> ids);
}
