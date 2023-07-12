package com.example.naejango.domain.user.dto.request;

import com.example.naejango.domain.user.domain.Gender;
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

    @NotEmpty(message = "나이는 필수 입력값 입니다.")
    @Size(min = 1, max = 200, message = "올바른 나이를 입력하세요")
    private int age;

    @NotNull
    private Gender gender;

    @NotBlank
    @Length(min = 2, max = 10)
    private String nickname;

    @NotNull
    @Length(max = 200)
    private String intro;

    @NotEmpty
    @Length(min = 10, max = 11)
    private String phoneNumber;

    @NotNull
    @Length(max = 100)
    private String imgUrl;
}
