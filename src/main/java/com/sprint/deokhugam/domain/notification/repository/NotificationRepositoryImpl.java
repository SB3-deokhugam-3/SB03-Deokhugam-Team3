package com.sprint.deokhugam.domain.notification.repository;

import static com.sprint.deokhugam.domain.notification.entity.QNotification.notification;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.deokhugam.domain.notification.entity.Notification;
import com.sprint.deokhugam.domain.notification.entity.QNotification;
import com.sprint.deokhugam.global.dto.response.CursorPageResponse;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    @Override
    public CursorPageResponse<Notification> findByUserIdWithCursor(UUID userId, Instant after, UUID cursor, int limit) {
        //  Notification 바탕으로 자동 생성한 쿼리타입
        QNotification notification = QNotification.notification;

        // 유저 ID가 일치하고, 생성 시간이 after 보다 이전인 알림만 조회하겠다는 조건
        BooleanExpression where = notification.user.id.eq(userId)
                .and(notification.createdAt.before(after));

        // 커서가 있을 경우, 조건 추가
        if (cursor != null) {
            where = where.and(notification.id.lt(cursor));
        }

        List<Notification> results = queryFactory
                .selectFrom(notification)
                .where(where)
                .orderBy(notification.createdAt.desc(), notification.id.desc())
                .limit(limit + 1)
                .fetch();

        for (Notification n : results) {
            em.detach(n); // 1차 캐시에서 강제로 떼어내기

        }
        log.info("[NotificationRepository] userId: {}, 알림 개수: {}", userId, results.size());

        boolean hasNext = results.size() > limit;

        if (hasNext) {
            results = results.subList(0, limit);
        }

        String nextCursor = null;
        String nextAfter = null;

        if (hasNext) {
            Notification last = results.get(results.size() - 1);
            nextCursor = last.getId().toString();
            nextAfter = last.getCreatedAt().toString();
        }

        return new CursorPageResponse<>(
                results,
                nextCursor,
                nextAfter,
                results.size(),
                null,
                hasNext
        );
    }


    @Transactional
    @Override
    public void markAllAsReadByUserId(UUID userId) {
        queryFactory
                .update(notification)
                .set(notification.isConfirmed, true)
                .set(notification.updatedAt, Instant.now())
                .where(notification.user.id.eq(userId)
                        .and(notification.isConfirmed.eq(false)))
                .execute();

        em.flush();
        em.clear();
    }
}