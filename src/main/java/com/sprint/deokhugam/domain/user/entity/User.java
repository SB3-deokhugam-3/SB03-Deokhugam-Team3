package com.sprint.deokhugam.domain.user.entity;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {

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
