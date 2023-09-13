package com.example.naejango.domain.user.domain;

import com.example.naejango.domain.common.TimeAuditingEntity;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Table(name = "userprofile")
@EntityListeners(AuditingEntityListener.class)
public class UserProfile extends TimeAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userprofile_id")
    private Long id;
    @Column(nullable = false)
    private String nickname;
    @Column
    @Length(max = 1500)
    private String intro;
    @Column
    @Length(max = 100)
    private String imgUrl;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String birth;

    @Column(nullable = false)
    private String phoneNumber;
    @Column
    private LocalDateTime lastLogin;

    /*
     * 회원간 오프라인 거래가 주 서비스이므로
     * 회원 정보(핸드폰 번호, 성별, 연령대) 기입이 필수적임
     * 회원 가입 시 또는 서비스 이용시
     * 회원정보가 미 기입되었는지 판별할 필요가 있음
     */
    public void modifyUserProfile(String nickname, String intro, String imgUrl) {
        this.nickname = nickname;
        this.intro = intro;
        this.imgUrl = imgUrl;
    }

    public void setLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

}
