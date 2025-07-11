package com.sprint.deokhugam.domain.review.repository;

import com.sprint.deokhugam.domain.review.entity.Review;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

}
