package com.example.naejango.domain.user.dto.response;

import com.example.naejango.domain.user.domain.Gender;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MyProfileResponseDto {

    private Long userId;
    private String nickname;
    private String intro;
    private String imgUrl;
    private Gender gender;
    private String birth;
    private String phoneNumber;
    private int balance;

}
