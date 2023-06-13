package com.example.naejango.domain.user.dto.request;

import lombok.Data;

@Data
public class UserInfoModifyRequest {
    private String nickname;
    private String profileImageUrl;
    private String intro;
}
