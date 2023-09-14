package com.example.naejango.domain.user.dto;

import com.example.naejango.domain.user.dto.request.ModifyUserProfileRequestDto;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ModifyUserProfileCommandDto {
    private Long userId;
    private String nickname;
    private String intro;
    private String imgUrl;

    public ModifyUserProfileCommandDto(Long userId, ModifyUserProfileRequestDto requestDto) {
        this.userId = userId;
        this.nickname = requestDto.getNickname();
        this.intro = requestDto.getIntro();
        this.imgUrl = requestDto.getImgUrl();
    }
}
