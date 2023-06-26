package com.example.naejango.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoModifyRequest {

    private String nickname;

    private String intro;

    private String imgUrl;

}
