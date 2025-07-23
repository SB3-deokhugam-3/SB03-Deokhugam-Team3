package com.sprint.deokhugam.domain.popularreview.service;

import com.sprint.deokhugam.domain.book.storage.s3.S3Storage;
import com.sprint.deokhugam.domain.popularreview.PeriodType;
import com.sprint.deokhugam.domain.popularreview.dto.data.PopularReviewDto;
import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.domain.popularreview.mapper.PopularReviewMapper;
import com.sprint.deokhugam.domain.popularreview.repository.PopularReviewRepository;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
//
//    @Override
//    @Transactional
//    public void updatePopularReviews(PeriodType period) {
//        Instant now = Instant.now();
//        Instant from = period == PeriodType.ALL_TIME ? Instant.EPOCH : now.minus(period.getDays(), ChronoUnit.DAYS);
//
//        log.info("[popularReview] 랭킹 계산 시작 - 기간: {}, from: {}, to: {}", period, from, now);
//
//        // 1. 점수 계산 및 정렬된 통계 가져오기
//        List<PopularReviewStats> stats = popularReviewRepository.calculatePopularReviews(from, now);
//        log.info("[popularReview] 통계 조회 완료 - {}개 리뷰 대상", stats.size());
//
//        // 2. 점수 기준으로 정렬 (이미 정렬되어 있다면 생략 가능)
//        stats.sort(Comparator.comparingDouble(PopularReviewStats::score).reversed());
//        log.debug("[popularReview] Top5 점수 샘플: {}",
//            stats.stream().limit(5).map(PopularReviewStats::score).toList()
//        );
//
//        // 3. PopularReviewService 엔티티 생성 (순위는 정렬된 순서대로)
//        AtomicLong rank = new AtomicLong(1);
//        List<PopularReview> popularReviews = stats.stream()
//            .map(stat -> {
//                UUID reviewId = stat.reviewId();
//                Review review = reviewRepository.getReferenceById(reviewId); // Lazy reference
//
//                return PopularReview.builder()
//                    .review(review)
//                    .period(period)
//                    .rank(rank.getAndIncrement())
//                    .score(stat.score())
//                    .likeCount(stat.likeCount())
//                    .commentCount(stat.commentCount())
//                    .build();
//            })
//            .toList();
//
//        // 4. 기존 데이터 삭제
//        popularReviewRepository.deleteByPeriod(period);
//        log.info("[popularReview] 기존 랭킹 삭제 완료 - period={}", period);
//
//        // 5. 새로 저장
//        popularReviewRepository.saveAll(popularReviews);
//        log.info("[popularReview] 새 랭킹 저장 완료 - {}건 저장됨", popularReviews.size());
//    }

}