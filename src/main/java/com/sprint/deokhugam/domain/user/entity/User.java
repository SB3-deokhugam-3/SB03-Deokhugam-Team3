package com.sprint.deokhugam.domain.user.entity;

import com.sprint.deokhugam.global.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("is_deleted = false")
public class User extends BaseUpdatableEntity {

    @Column(length = 320, nullable = false, unique = true)
    private String email;

    @Column(length = 50, nullable = false)
    private String nickname;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Builder
    public User(String email, String nickname, String password, boolean isDeleted) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.isDeleted = isDeleted;
    }

    public void update(String newNickname) {
        if (newNickname != null && !newNickname.equals(this.nickname)) {
            this.nickname = newNickname;
        }
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = Instant.now();
    }

}
