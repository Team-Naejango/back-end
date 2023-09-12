package com.example.naejango.domain.user.dto;

import com.example.naejango.domain.user.domain.Gender;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserProfileDto {

    private String nickname;
    private String intro;
    private String imgUrl;
    private Gender gender;
    private String birth;
    private String phoneNumber;
    private LocalDateTime lastLogin;

}
