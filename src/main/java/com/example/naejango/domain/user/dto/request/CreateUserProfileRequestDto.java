package com.example.naejango.domain.user.dto.request;

import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.global.common.validation.EnumConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserProfileRequestDto {

    @NotEmpty
    @Size(min = 8, max = 8, message = "올바른 생년월일을 입력하세요. (yyyymmdd)")
    private String birth;

    @EnumConstraint(enumClass = Gender.class,
            message = "올바른 성별 값을 입력하세요. (남/여)")
    private Gender gender;

    @NotBlank
    @Length(min = 2, max = 10)
    private String nickname;

    @NotNull
    @Length(max = 200)
    private String intro;

    @NotEmpty
    @Length(min = 11, max = 11, message = "올바른 전화번호를 입력하세요 (01012345678")
    private String phoneNumber;

    @NotNull
    @Length(max = 100)
    private String imgUrl;
}
