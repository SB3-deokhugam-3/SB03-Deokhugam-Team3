package com.sprint.deokhugam.domain.user.repository;

import com.sprint.deokhugam.domain.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
