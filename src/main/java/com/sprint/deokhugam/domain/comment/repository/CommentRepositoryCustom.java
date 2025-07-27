package com.sprint.deokhugam.domain.comment.repository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface CommentRepositoryCustom {

    Map<UUID, Long> countByReviewIdBetween(Instant start, Instant end);
}
