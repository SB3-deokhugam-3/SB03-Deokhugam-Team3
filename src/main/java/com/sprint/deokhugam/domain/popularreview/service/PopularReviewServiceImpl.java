package com.sprint.deokhugam.domain.popularreview.service;

import com.sprint.deokhugam.global.storage.S3Storage;
import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.popularreview.dto.data.ReviewScoreDto;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.domain.popularreview.mapper.PopularReviewMapper;
import com.sprint.deokhugam.domain.popularreview.repository.PopularReviewRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.enums.PeriodType;
import com.sprint.deokhugam.global.exception.BatchAlreadyRunException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopularReviewServiceImpl implements PopularReviewService {

    private final PopularReviewRepository popularReviewRepository;
    private final ReviewRepository reviewRepository;
    private final S3Storage s3Storage;
    private final PopularReviewMapper popularReviewMapper;

    @Override
    public CursorPageResponse<PopularReviewDto> getPopularReviews(
        PeriodType period,
        Sort.Direction direction,
        String cursor,
        Instant after,
        int limit
    ) {

        List<PopularReview> entities = popularReviewRepository.findByPeriodWithCursor(
            period,
            direction,
            cursor,
            after,
            limit + 1
        );

        boolean hasNext = entities.size() > limit;
        if (hasNext) {
            entities = entities.subList(0, limit);
        }

        List<PopularReviewDto> dtos = entities.stream()
            .map(entity -> popularReviewMapper.toDto(entity, s3Storage))
            .toList();

        // 커서 생성
        String nextCursor = hasNext && !dtos.isEmpty()
            ? String.valueOf(dtos.get(dtos.size() - 1).rank())
            : null;

        return new CursorPageResponse<>(
            dtos,
            nextCursor,
            dtos.isEmpty() ? null : dtos.get(dtos.size() - 1).createdAt().toString(),
            dtos.size(),
            null,
            hasNext
        );
    }

    /* 배치에서 사용 - 오늘 이미 실행한 배치인지 검증 */
    public void validateJobNotDuplicated(Instant referenceTime)
        throws BatchAlreadyRunException {
        // 모든 기간에 대해 체크 ( referenceTime 기준으로 )
        for (PeriodType period : PeriodType.values()) {
            long existingCount = popularReviewRepository.countByPeriodAndCreatedDate(period, referenceTime);
            if (existingCount > 0) {
                log.info("기간 {} 배치 이미 실행됨 - 날짜: {}, {}건 존재",
                    period, referenceTime, existingCount);
                throw new BatchAlreadyRunException("PopularReview",
                    Map.of(
                        "period", period.toString(),
                        "reference_date", referenceTime.toString(),
                        "existing_count", existingCount,
                        "execution_datetime", Instant.now()
                    ));
            }
        }
        log.info("배치 중복 검증 통과 - 날짜: {}", referenceTime);
    }

    /* 배치에서 사용 */
    @Transactional
    public List<PopularReview> savePopularReviewsByPeriod(PeriodType period, Instant today,
        Map<UUID, Long> commentMap, Map<UUID, Long> likeMap, StepContribution contribution) {

        Set<UUID> reviewIds = new HashSet<>();
        reviewIds.addAll(commentMap.keySet());
        reviewIds.addAll(likeMap.keySet());

        List<Review> reviews = reviewRepository.findAllByIdInAndIsDeletedFalse(reviewIds);

        AtomicLong rank = new AtomicLong(1);

        List<PopularReview> result = reviews.stream()
            .map(r -> {
                UUID id = r.getId();
                long c = commentMap.getOrDefault(id, 0L);
                long l = likeMap.getOrDefault(id, 0L);
                double score = c * 0.7 + l * 0.3;
                return new ReviewScoreDto(r, c, l, score);
            })
            .filter(s -> s.score() > 0)
            .sorted(Comparator
                .comparingDouble(ReviewScoreDto::score).reversed()
                .thenComparing(ReviewScoreDto::commentCount, Comparator.reverseOrder())
                .thenComparing(s -> s.review().getCreatedAt())
            )
            .map(s -> PopularReview.builder()
                .period(period)
                .rank(rank.getAndIncrement())
                .score(s.score())
                .likeCount(s.likeCount())
                .commentCount(s.commentCount())
                .review(s.review())
                .build())
            .toList();

        popularReviewRepository.saveAll(result);
        //batch meta 테이블에 결과 저장
        contribution.incrementWriteCount(result.size());

        return result;
    }

    @Override
    public Double getUserPopularityScoreSum(UUID userId, PeriodType period) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (period == null) {
            throw new IllegalArgumentException("기간 타입은 필수입니다.");
        }

        log.debug("사용자 인기 리뷰 점수 합계 조회 - userId: {}, period: {}", userId, period);

        Double scoreSum = popularReviewRepository.findScoreSumByUserIdAndPeriod(userId, period);

        log.debug("사용자 인기 리뷰 점수 합계 조회 결과 - userId: {}, period: {}, scoreSum: {}",
            userId, period, scoreSum);

        return scoreSum != null ? scoreSum : 0.0;
    }

    /**
     * 특정 기간과 날짜의 기존 인기 리뷰 데이터 조회
     */
    private List<PopularReview> getExistingPopularReviewsByPeriodAndDate(PeriodType period, Instant date) {
        return popularReviewRepository.findByPeriodAndCreatedDate(period, date);
    }
}