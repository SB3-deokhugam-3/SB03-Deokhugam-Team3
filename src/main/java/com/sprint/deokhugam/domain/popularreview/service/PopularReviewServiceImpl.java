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
import com.sprint.deokhugam.global.utils.TimeUtils;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
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
            ? generateCursor(dtos.get(dtos.size() - 1))
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

    private String generateCursor(PopularReviewDto dto) {
        return Base64.getEncoder().encodeToString(
            (dto.rank() + "|" + dto.createdAt().toString()).getBytes(StandardCharsets.UTF_8)
        );
    }

    /* 배치에서 사용 - 오늘 이미 실행한 배치인지 검증 */
    public void validateJobNotDuplicated(Instant referenceTime)
        throws BatchAlreadyRunException {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");

        Instant startOfDay = com.sprint.deokhugam.global.enums.PeriodType.DAILY.getStartInstant(referenceTime, zoneId);
        Instant endOfDay = com.sprint.deokhugam.global.enums.PeriodType.DAILY.getEndInstant(referenceTime, zoneId);

        System.out.println("startOfDay = " + startOfDay);
        System.out.println("endOfDay = " + endOfDay);

        Boolean isAlreadyExist = popularReviewRepository.existsByCreatedAtBetween(startOfDay,
            endOfDay);

        System.out.println("isAlreadyExist = " + isAlreadyExist);

        if (isAlreadyExist) {
            throw new BatchAlreadyRunException("Review",
                Map.of("execution datetime", Instant.now()));
        }
    }

    /* 배치에서 사용 */
    @Transactional
    public List<PopularReview> savePopularReviewsByPeriod(List<Review> totalReviews,
        PeriodType period, StepContribution contribution, Instant today) {
        List<PopularReview> popularReviews = new ArrayList<>();
        List<Review> slicedReview;
        Long rank = 1L;
        LocalDate startLocalDate;

        switch (period) {
            case DAILY:
                startLocalDate = TimeUtils.toLocalDate(today).minusDays(1);
                slicedReview = totalReviews.stream()
                    .filter(review -> {
                        LocalDate reviewDate = TimeUtils.toLocalDate(review.getCreatedAt());
                        return reviewDate.isEqual(startLocalDate);
                    }).toList();
                break;
            case WEEKLY:
                startLocalDate = TimeUtils.toLocalDate(today).minusWeeks(1);
                slicedReview = totalReviews.stream()
                    .filter(review -> {
                        LocalDate reviewDate = TimeUtils.toLocalDate(review.getCreatedAt());
                        return !reviewDate.isBefore(startLocalDate);
                    }).toList();

                break;
            case MONTHLY:
                startLocalDate = TimeUtils.toLocalDate(today).minusMonths(1);
                slicedReview = totalReviews.stream()
                    .filter(review -> {
                        LocalDate reviewDate = TimeUtils.toLocalDate(review.getCreatedAt());
                        return !reviewDate.isBefore(startLocalDate);
                    }).toList();
                break;
            case ALL_TIME:
            default:
                slicedReview = totalReviews;
                break;
        }

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
        //batch meda 테이블에 결과 저장
        contribution.incrementWriteCount(popularReviews.size());

        return popularReviews;
    }


}