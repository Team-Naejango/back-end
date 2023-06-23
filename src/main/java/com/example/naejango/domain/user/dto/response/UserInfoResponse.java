package com.example.naejango.domain.user.dto.response;

import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.UserProfile;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserInfoResponse {
    private String nickname;
    private String imgUrl;
    private int age;
    private Gender gender;
    private String intro;

    @Builder
    public UserInfoResponse(UserProfile userProfile) {
        this.nickname =  userProfile.getNickname();
        this.imgUrl = userProfile.getImgUrl();
        this.age = userProfile.getAge();
        this.gender = userProfile.getGender();
        this.intro = userProfile.getIntro();
    }
}
