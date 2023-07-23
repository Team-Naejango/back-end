package com.example.naejango.domain.user.domain;

import com.example.naejango.domain.storage.domain.Storage;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="users")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String userKey;

    @Column(nullable = false)
    private String password;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Column
    private String signature;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userprofile_id")
    private UserProfile userProfile;

    @OneToMany(mappedBy = "user")
    private List<Storage> storages;

    public void allocateStorage(Storage storage) {
        this.storages.add(storage);
    }

    public void createUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        this.role = Role.USER;
    }

    public void refreshSignature(String signature) {
        this.signature = signature;
    }

}
