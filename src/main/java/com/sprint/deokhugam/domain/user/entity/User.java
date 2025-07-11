package com.sprint.deokhugam.domain.user.entity;

import com.sprint.deokhugam.global.base.BaseEntity;
import com.sprint.deokhugam.global.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseUpdatableEntity {

    @Column(length = 20, nullable = false, unique = true)
    private String email;

    @Column(length = 20, nullable = false, unique = true)
    private String nickname;

    @Column(length = 20, nullable = false)
    private String password;

    public void update(String newNickname) {
        if(newNickname != null && !newNickname.equals(this.nickname)){
            this.nickname = newNickname;
        }
    }

}
