package com.example.naejango.domain.user.dto.response;

import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.UserProfile;
import lombok.Builder;
import lombok.Data;

@Data
public class UserInfoResponse {
    private String nickname;
    private String profileImageUrl;
    private int age;
    private Gender gender;
    private String intro;

    @Builder
    public UserInfoResponse(UserProfile userProfile) {
        this.nickname =  userProfile.getNickname();
        this.profileImageUrl = userProfile.getImgUrl();
        this.age = userProfile.getAge();
        this.gender = userProfile.getGender();
        this.intro = userProfile.getIntro();
    }
}
