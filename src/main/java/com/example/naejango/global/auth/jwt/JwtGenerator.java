package com.example.naejango.global.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.naejango.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class JwtGenerator {
    /**
     * AccessToken을 생성하는 메서드
     * 해싱 알고리즘(HMAC512)으로 jwt를 생성합니다.
     *
     * @return JwtAccessToken
     */
    public String generateAccessToken(User user) {
        return JWT.create()
                .withClaim("userKey", user.getUserKey())
                .withExpiresAt(LocalDateTime.now().plusMinutes(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME).toInstant(ZoneOffset.of("+9")))
                .withIssuer(JwtProperties.ISS)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));
    }

    /**
     * RefreshToken을 생성하는 메서드
     * 해싱 알고리즘(HMAC512)으로 jwt를 생성합니다.

     * @return JwtRefreshToken
     */
    public String generateRefreshToken(User user) {
            return JWT.create()
                    .withClaim("userKey", user.getUserKey())
                    .withExpiresAt(LocalDateTime.now().plusDays(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME).toInstant(ZoneOffset.of("+9")))
                    .withIssuer(JwtProperties.ISS)
                    .sign(Algorithm.HMAC512(JwtProperties.SECRET));
    }
}
