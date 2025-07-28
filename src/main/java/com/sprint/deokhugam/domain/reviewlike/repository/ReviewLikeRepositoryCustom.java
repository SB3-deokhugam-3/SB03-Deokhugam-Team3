package com.sprint.deokhugam.domain.reviewlike.repository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface ReviewLikeRepositoryCustom {

    Map<UUID, Long> countByReviewIdBetween(Instant start, Instant end);
}
