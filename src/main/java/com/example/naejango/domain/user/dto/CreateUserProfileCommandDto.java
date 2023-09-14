package com.example.naejango.domain.user.dto;

import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.dto.request.CreateUserProfileRequestDto;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class CreateUserProfileCommandDto {

    private Long userId;
    private String phoneNumber;
    private String imgUrl;
    private String nickname;
    private String intro;
    private String birth;
    private Gender gender;

    public CreateUserProfileCommandDto(Long userId, CreateUserProfileRequestDto createUserProfileRequestDto) {
        this.userId = userId;
        this.phoneNumber = createUserProfileRequestDto.getPhoneNumber();
        this.imgUrl = createUserProfileRequestDto.getImgUrl();
        this.nickname = createUserProfileRequestDto.getNickname();
        this.intro = createUserProfileRequestDto.getIntro();
        this.birth = createUserProfileRequestDto.getBirth();
        this.gender = createUserProfileRequestDto.getGender();
    }

    public UserProfile toEntity() {
        return UserProfile.builder()
                .nickname(this.nickname)
                .intro(this.intro)
                .imgUrl(this.imgUrl)
                .gender(this.gender)
                .phoneNumber(this.phoneNumber)
                .birth(this.birth)
                .lastLogin(LocalDateTime.now())
                .build();
    }
}
