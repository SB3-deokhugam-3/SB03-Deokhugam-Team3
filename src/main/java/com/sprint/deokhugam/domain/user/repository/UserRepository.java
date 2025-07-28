package com.sprint.deokhugam.domain.user.repository;

import com.sprint.deokhugam.domain.user.entity.User;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT * FROM users WHERE is_deleted = true AND deleted_at <= :cutoff", nativeQuery = true)
    List<User> findDeletableUsers(@Param("cutoff") Instant cutoff);

    @Modifying
    @Query(value = "DELETE FROM users WHERE id IN (:userIds)", nativeQuery = true)
    @Transactional
    int deleteByIdIn(@Param("userIds") List<UUID> userIds);
}
