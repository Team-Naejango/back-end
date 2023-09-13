package com.example.naejango.domain.user.dto.response;

import com.example.naejango.domain.user.domain.Gender;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Getter @Setter
@NoArgsConstructor
public class UserProfileResponseDto {
    private String nickname;
    private String imgUrl;
    private String age;
    private String gender;
    private String intro;
    private String lastLogin;

    @Builder
    public UserProfileResponseDto(String nickname, String imgUrl, String birth, Gender gender, String intro, LocalDateTime lastLogin) {
        this.nickname =  nickname;
        this.imgUrl = imgUrl;
        LocalDate birthDate = LocalDate.parse(birth, DateTimeFormatter.ofPattern("yyyyMMdd"));
        this.age = (Period.between(birthDate, LocalDate.now()).getYears() / 10) * 10 + "대";
        this.gender = gender.getGender();
        this.intro = intro;
        Duration duration = Duration.between(lastLogin, LocalDateTime.now());
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();
        long weeks = days / 7;

        String timeAgo = "";
        if (weeks >= 1) {
            timeAgo = weeks + "주 전";
        } else if (days >= 1) {
            timeAgo = days + "일 전";
        } else if (hours >= 1) {
            timeAgo = hours + "시간 전";
        } else if (minutes >= 1) {
            timeAgo = minutes + "분 전";
        } else {
            timeAgo = "방금";
        }
        this.lastLogin = timeAgo;
    }
}
