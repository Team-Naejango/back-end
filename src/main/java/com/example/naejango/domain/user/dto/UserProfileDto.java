package com.example.naejango.domain.user.dto;

import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.UserProfile;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class UserProfileDto {

    private String nickname;
    private String intro;
    private String imgUrl;
    private Gender gender;
    private String birth;
    private String phoneNumber;
    private LocalDateTime lastLogin;

    public UserProfileDto(UserProfile userProfile) {
        this.nickname = userProfile.getNickname();
        this.intro = userProfile.getIntro();
        this.imgUrl = userProfile.getImgUrl();
        this.gender = userProfile.getGender();
        this.birth = userProfile.getBirth();
        this.phoneNumber = userProfile.getPhoneNumber();
        this.lastLogin = userProfile.getLastLogin();
    }
}
