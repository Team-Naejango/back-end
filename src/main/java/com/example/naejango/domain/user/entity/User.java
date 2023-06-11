package com.example.naejango.domain.user.entity;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userKey;

    @Column
    private String password;

    @Column
    private String nickname;

    @Column
    private String phoneNumber;

    @Column
    private Gender gender;

    @Column
    private Integer age;

    @Column
    @ColumnDefault("0")
    private int point;

    @Column
    @ColumnDefault("0")
    private int rate;

    @Setter
    @Column
    private String signature;

    @Enumerated(value = EnumType.STRING)
    @Column
    private Role role;

    /**
     * 회원간 오프라인 거래가 주 서비스이므로
     * 회원 정보(핸드폰 번호, 성별, 연령대) 기입이 필수적임
     * 회원 가입 시 또는 서비스 이용시
     * 회원정보가 미 기입되었는지 판별할 필요가 있음
     */
    public boolean isCompleteInfo(){
        return phoneNumber != null && gender != null && age != null;
    }



//    @OneToMany(mappedBy = "user")
//    private List<Item> items = new ArrayList<>();
}
