package com.sprint.deokhugam.domain.reviewlike.service;

import com.sprint.deokhugam.domain.reviewlike.dto.data.ReviewLikeDto;
import java.util.UUID;

public interface ReviewLikeService {

    ReviewLikeDto toggleLike(UUID reviewId, UUID userId, boolean liked);

}
