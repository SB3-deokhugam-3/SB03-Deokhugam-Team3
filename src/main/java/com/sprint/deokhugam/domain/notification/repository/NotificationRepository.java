package com.sprint.deokhugam.domain.notification.repository;

import com.sprint.deokhugam.domain.notification.entity.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

    @Modifying
    @Query(value = """
        DELETE FROM notifications 
        WHERE review_id IN (
            SELECT id FROM reviews WHERE user_id IN (:userIds)
        )
        """, nativeQuery = true)
    void deleteByReviewUserIdIn(@Param("userIds") List<UUID> userIds);
}