package com.example.naejango.domain.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name="userprofile")
@EntityListeners(AuditingEntityListener.class)
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String nickname;

    @Column
    private String intro;

    @Column(nullable = false)
    private String phoneNumber;

    @ColumnDefault("0")
    @Column(nullable = false)
    private int rate;

    @Column
    private String imgUrl;

    @Column
    private Timestamp lastLogin;

    @Column
    @CreatedDate
    private Timestamp createdAt;


    /**
     * 회원간 오프라인 거래가 주 서비스이므로
     * 회원 정보(핸드폰 번호, 성별, 연령대) 기입이 필수적임
     * 회원 가입 시 또는 서비스 이용시
     * 회원정보가 미 기입되었는지 판별할 필요가 있음
     */
    public boolean isCompleteProfile(){
        return phoneNumber != null && gender != null && age != null;
    }

    public void modifyUserProfile(String nickname, String intro, String phoneNumber) {
        this.nickname = nickname;
        this.intro = intro;
        this.phoneNumber = phoneNumber;
    }

    public void setLastLogin() {
        this.lastLogin = new Timestamp(System.currentTimeMillis());
    }

}
