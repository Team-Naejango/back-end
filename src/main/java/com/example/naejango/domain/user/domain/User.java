package com.example.naejango.domain.user.domain;

import com.example.naejango.domain.common.TimeAuditingEntity;
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
public class User extends TimeAuditingEntity {
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
    private String refreshToken;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userprofile_id")
    private UserProfile userProfile;

    @OneToMany(mappedBy = "user")
    private List<Storage> storages;

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        if(this.role == Role.TEMPORAL) this.role = Role.USER;
    }

}
