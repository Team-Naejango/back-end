package com.example.naejango.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifyUserProfileRequestDto {

    @NotBlank
    @Size(min = 2, max = 10)
    private String nickname;

    @NotNull
    @Size(max = 200)
    private String intro;

    @NotNull
    @Size(max = 100)
    private String imgUrl;

}
