package com.example.naejango.domain.user.dto.request;

import com.example.naejango.domain.user.entity.Role;
import com.example.naejango.domain.user.entity.User;
import lombok.Data;

@Data
public class RequestJoinDto {
    private String email;
    private String password;
    private String nickname;
    private String phoneNumber;

    public User toEntity(String password) {
        return User.builder()
                .id(null)
                .password(password)
                .nickname(nickname)
                .phoneNumber(phoneNumber)
                .point(0)
                .rate(0)
                .role(Role.USER)
                .build();
    }
}
