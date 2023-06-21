package com.example.naejango.domain.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Builder
@Table(name="users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userKey;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String signature;

    @OneToOne(mappedBy = "user")
    private UserProfile userProfile;

    public void setSignature(String signature) {
        this.signature = signature;
    }

}
