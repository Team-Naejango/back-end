package com.example.naejango.domain.user.dto.response;

import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.UserProfile;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifyUserProfileResponseDto {
    private String nickname;
    private String imgUrl;
    private String birth;
    private String phoneNumber;
    private Gender gender;
    private String intro;

    @Builder
    public ModifyUserProfileResponseDto(UserProfile userProfile) {
        this.nickname =  userProfile.getNickname();
        this.imgUrl = userProfile.getImgUrl();
        this.birth = userProfile.getBirth();
        this.phoneNumber = userProfile.getPhoneNumber();
        this.gender = userProfile.getGender();
        this.intro = userProfile.getIntro();
    }
}
