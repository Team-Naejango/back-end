package com.example.naejango.domain.user.dto.response;

import com.example.naejango.domain.user.entity.Gender;
import com.example.naejango.domain.user.entity.User;


public class UserInfoResponse {
    private final String nickname;
    private final String profileImageUrl;
    private final int age;
    private final Gender gender;
    private final String intro;

    public UserInfoResponse(User user) {
        this.nickname =  user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.age = user.getAge();
        this.gender = user.getGender();
        this.intro = user.getIntro();
    }
}
