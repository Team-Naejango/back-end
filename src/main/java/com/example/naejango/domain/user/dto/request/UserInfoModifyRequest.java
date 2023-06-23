package com.example.naejango.domain.user.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoModifyRequest {

    private String nickname;

    private String intro;

    private String imgUrl;

}
