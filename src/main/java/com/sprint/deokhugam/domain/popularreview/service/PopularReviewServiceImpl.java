package com.sprint.deokhugam.domain.popularreview.service;

import com.sprint.deokhugam.domain.book.storage.s3.S3Storage;
import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.domain.popularreview.mapper.PopularReviewMapper;
import com.sprint.deokhugam.domain.popularreview.repository.PopularReviewRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import com.sprint.deokhugam.global.enums.PeriodType;
import com.sprint.deokhugam.global.exception.BatchAlreadyRunException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
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

    @Transactional
    public List<PopularReview> savePopularReviewsByPeriod(List<Review> totalReviews,
        PeriodType period, StepContribution contribution, Instant today) {

        // 해당 기간과 날짜에 대한 중복 검증
        long existingCount = popularReviewRepository.countByPeriodAndCreatedDate(period, today);
        if (existingCount > 0) {
            log.info("기간 {} 날짜 {} 인기 리뷰 데이터가 이미 존재합니다. ({}건) 배치 건너뜀",
                period, today, existingCount);

            // 해당 날짜의 기존 데이터 반환
            return getExistingPopularReviewsByPeriodAndDate(period, today);
        }

        log.info("기간 {} 날짜 {} 인기 리뷰 데이터 생성 시작", period, today);

        List<PopularReview> popularReviews = new ArrayList<>();
        List<Review> slicedReview;
        Long rank = 1L;
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        Instant start;
        Instant end;

        switch (period) {
            case DAILY:
                start = PeriodType.DAILY.getStartInstant(today, zoneId);
                end = PeriodType.DAILY.getEndInstant(today, zoneId);
                slicedReview = totalReviews.stream()
                    .filter(review -> isBetween(review.getCreatedAt(), start, end))
                    .toList();
                break;
            case WEEKLY:
                start = PeriodType.WEEKLY.getStartInstant(today, zoneId);
                end = PeriodType.WEEKLY.getEndInstant(today, zoneId);
                slicedReview = totalReviews.stream()
                    .filter(review -> isBetween(review.getCreatedAt(), start, end))
                    .toList();
                break;
            case MONTHLY:
                start = PeriodType.MONTHLY.getStartInstant(today, zoneId);
                end = PeriodType.MONTHLY.getEndInstant(today, zoneId);
                slicedReview = totalReviews.stream()
                    .filter(review -> isBetween(review.getCreatedAt(), start, end))
                    .toList();
                break;
            case ALL_TIME:
            default:
                slicedReview = totalReviews;
                break;
        }

        // 점수순으로 정렬 (높은 점수부터)
        slicedReview = slicedReview.stream()
            .sorted((r1, r2) -> {
                double score1 = r1.getCommentCount() * 0.7 + r1.getLikeCount() * 0.3;
                double score2 = r2.getCommentCount() * 0.7 + r2.getLikeCount() * 0.3;
                return Double.compare(score2, score1); // 내림차순
            })
            .toList();

        for (Review review : slicedReview) {
            Long commentCount = review.getCommentCount();
            Long likeCount = review.getLikeCount();
            Double score = commentCount * 0.7 + likeCount * 0.3;
            PopularReview popularReview = PopularReview.builder()
                .period(period)
                .rank(rank)
                .score(score)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .review(review)
                .build();
            popularReviews.add(popularReview);
            rank++;
        }

        popularReviewRepository.saveAll(popularReviews);
        contribution.incrementWriteCount(popularReviews.size());

        return popularReviews;
    }

    private Boolean isBetween(Instant referenceTime, Instant start, Instant end) {
        // startTime 포함, endTime 미포함
        return !referenceTime.isBefore(start) && referenceTime.isBefore(end);
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