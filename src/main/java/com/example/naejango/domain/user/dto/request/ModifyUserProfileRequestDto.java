package com.example.naejango.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifyUserProfileRequestDto {

    @NotBlank
    @Length(min = 2, max = 10)
    private String nickname;

    @NotNull
    @Length(max = 200)
    private String intro;

    @NotNull
    @Length(max = 100)
    private String imgUrl;

}
