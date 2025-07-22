package com.sprint.deokhugam.domain.popularreview.service;

import com.sprint.deokhugam.domain.popularreview.entity.PopularReview;
import com.sprint.deokhugam.domain.popularreview.repository.PopularReviewRepository;
import com.sprint.deokhugam.domain.review.entity.Review;
import com.sprint.deokhugam.global.enums.PeriodType;
import com.sun.jdi.request.DuplicateRequestException;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularReviewService {

    private final PopularReviewRepository popularReviewRepository;

    /* 배치에서 사용 - 오늘 이미 실행한 배치인지 검증 */
    public void validateTodayJobNotDuplicated()
        throws DuplicateRequestException {
        ZoneId zoneId = ZoneId.systemDefault(); // 예: Asia/Seoul
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        // 오늘 시작 시간(00:00)
        ZonedDateTime startOfDayZdt = now.toLocalDate().atStartOfDay(zoneId);
        Instant startOfDay = startOfDayZdt.toInstant();
        // 오늘 종료 시간(23:59:59.999999999)
        ZonedDateTime endOfDayZdt = startOfDayZdt.plusDays(1).minusNanos(1);
        Instant endOfDay = endOfDayZdt.toInstant();

        Boolean isAlreadyExist = popularReviewRepository.existsByCreatedAtBetween(startOfDay,
            endOfDay);
        if (isAlreadyExist) {
            throw new DuplicateRequestException("오늘 이미 실행한 배치입니다.");
        }
    }

    /* 배치에서 사용 */
    @Transactional
    public void savePopularReviewsByPeriod(List<Review> totalReviews,
        PeriodType period, StepContribution contribution) {
        List<PopularReview> popularReviews = new ArrayList<>();
        List<Review> slicedReview;
        Long rank = 1L;
        //XXX : 시간은 제외해야하지않나?
        Instant endDate = Instant.now();
        Instant startDate;

        // 테스트코드에서 쓸것임
//        // create an Instant object
////        Instant instant
////            = Instant.parse(&quot;2018-12-30T19:34:50.63Z&quot;);
////
////        // subtract 20 DAYS to Instant
////        Instant value
////            = instant.minus(20, ChronoUnit.DAYS);
////
////        // print result
////        System.out.println(&quot;Instant after subtracting DAYS: &quot;
////        + value);

        switch (period) {
            case DAILY:
                startDate = ZonedDateTime.ofInstant(endDate, ZoneId.systemDefault())
                    .minusDays(1)
                    .toInstant();
                slicedReview = totalReviews.stream()
                    .filter(review -> review.getCreatedAt().isAfter(startDate)).toList();
                break;
            case WEEKLY:
                startDate = ZonedDateTime.ofInstant(endDate, ZoneId.systemDefault())
                    .minusWeeks(1)
                    .toInstant();
                slicedReview = totalReviews.stream()
                    .filter(review -> review.getCreatedAt().isAfter(startDate)).toList();
                break;
            case MONTHLY:
                startDate = ZonedDateTime.ofInstant(endDate, ZoneId.systemDefault())
                    .minusMonths(1)
                    .toInstant();
                slicedReview = totalReviews.stream()
                    .filter(review -> review.getCreatedAt().isAfter(startDate)).toList();
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
                .period(period.getValue())
                .rank(rank)
                .score(score)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .review(review)
                .build();
            popularReviews.add(popularReview);
            rank++;
        }

        System.out.println("createMonthlyPopularReviews.size= " + popularReviews.size());

        for (PopularReview popularReview : popularReviews) {
            System.out.println(period + "->popularReview.toString() = " + popularReview.toString());
        }

        popularReviewRepository.saveAll(popularReviews);
        // batch meda 테이블에 결과 저장
        contribution.incrementWriteCount(popularReviews.size());
    }


}
