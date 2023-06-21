package com.example.naejango.domain.user.dto.request;

import lombok.Data;

@Data
public class UserInfoModifyRequest {

    private Integer age;

    private String nickname;

    private String intro;

    private String phoneNumber;

    private String imgUrl;

}
