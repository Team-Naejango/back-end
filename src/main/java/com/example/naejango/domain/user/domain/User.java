package com.example.naejango.domain.user.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Table(name="user")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userKey;

    @Column(nullable = false)
    private String password;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Column(nullable = false)
    private String signature;

    @OneToOne
    @JoinColumn(name = "id")
    private UserProfile userProfile;

    public void updateProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
