package com.example.naejango.global.auth.jwt;

import com.example.naejango.domain.user.domain.Role;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtPayload {
    private Long userId;
    private Role role;
}
