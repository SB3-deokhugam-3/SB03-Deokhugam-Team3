package com.sprint.deokhugam.domain.user.batch;

import com.sprint.deokhugam.domain.book.repository.BookRepository;
import com.sprint.deokhugam.domain.comment.repository.CommentRepository;
import com.sprint.deokhugam.domain.notification.repository.NotificationRepository;
import com.sprint.deokhugam.domain.review.repository.ReviewRepository;
import com.sprint.deokhugam.domain.reviewlike.repository.ReviewLikeRepository;
import com.sprint.deokhugam.domain.user.entity.User;
import com.sprint.deokhugam.domain.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UserCleanupBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final BookRepository bookRepository;

    @Bean
    public Job userCleanupJob() {
        return new JobBuilder("userCleanupJob", jobRepository)
            .start(userCleanupStep())
            .build();
    }

    @Bean
    public Step userCleanupStep() {
        return new StepBuilder("userCleanupStep", jobRepository)
            .<User, User>chunk(10, transactionManager) // 10개씩 처리
            .reader(userReader())
            .processor(userProcessor())
            .writer(userWriter())
            .build();
    }

    @Bean
    @StepScope
    public ItemReader<User> userReader() {
        return new ItemReader<User>() {
            private List<User> users;
            private int currentIndex = 0;
            private boolean initialized = false;

            @Override
            public User read() {
                if (!initialized) {
                    // 테스트: 10초 전, 실제: 1일 전
//                    Instant cutoff = Instant.now().minusSeconds(10);
                    Instant cutoff = Instant.now().minus(1, ChronoUnit.DAYS);

                    users = userRepository.findDeletableUsers(cutoff);
                    initialized = true;
                    log.info("삭제 대상 유저 {} 명 발견", users.size());
                }

                if (currentIndex < users.size()) {
                    return users.get(currentIndex++);
                }
                return null; // 더 이상 읽을 데이터 없음
            }
        };
    }

    @Bean
    public ItemProcessor<User, User> userProcessor() {
        return user -> {
            log.debug("유저 {} 처리 중", user.getId());
            return user; // 특별한 변환 없이 그대로 전달
        };
    }

    @Bean
    public ItemWriter<User> userWriter() {
        return chunk -> {
            List<UUID> userIds = chunk.getItems().stream()
                .map(User::getId)
                .toList();

            if (userIds.isEmpty()) {
                return;
            }

            log.info("유저 {} 명과 관련 데이터 삭제 시작", userIds.size());

            try {
                List<UUID> affectedBookIds = reviewRepository.findBookIdsByUserIdIn(userIds);

                // 1. 알림 삭제
                notificationRepository.deleteByReviewUserIdIn(userIds);
                log.debug("알림 데이터 삭제 완료");

                // 2. 댓글 삭제
                commentRepository.deleteByUserIdIn(userIds);
                log.debug("댓글 데이터 삭제 완료");

                // 3. 리뷰 좋아요 삭제
                reviewLikeRepository.deleteByUserIdIn(userIds);
                log.debug("리뷰 좋아요 데이터 삭제 완료");

                // 4. 리뷰 삭제
                reviewRepository.deleteAllByUserIdIn(userIds);
                log.debug("리뷰 데이터 삭제 완료");

                // 5. 유저 삭제
                userRepository.deleteByIdIn(userIds);
                log.debug("유저 데이터 삭제 완료");

                // 6. 모든 책의 카운트 재계산
                if (!affectedBookIds.isEmpty()) {
                    bookRepository.recalculateBookCounts(affectedBookIds); // 특정 책들만
                }
                reviewRepository.recalculateReviewCommentCounts(userIds);

                log.debug("카운트 재계산 완료");

                log.info("성공적으로 {} 명의 유저와 관련 데이터를 삭제했습니다.", userIds.size());

            } catch (Exception e) {
                log.error("유저 배치 삭제 중 오류 발생: {}", e.getMessage(), e);
                throw e;
            }
        };
    }

}
