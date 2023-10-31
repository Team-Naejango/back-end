package com.example.naejango.global.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class JwtGenerator {
    /**
     * AccessToken을 생성하는 메서드
     * 해싱 알고리즘(HMAC512)으로 jwt를 생성합니다.
     * @return JwtAccessToken
     */
    public String generateAccessToken(Long userId) {
        return JWT.create()
                .withClaim("userId", userId)
                .withExpiresAt(LocalDateTime.now().plusMinutes(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME).toInstant(ZoneOffset.of("+9")))
                .withIssuer(JwtProperties.ISS)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET_A));
    }

    public String generateAccessToken(Long userId, Duration duration){
        return JWT.create()
                .withClaim("userId", userId)
                .withExpiresAt(LocalDateTime.now().plus(duration).toInstant(ZoneOffset.of("+9")))
                .withIssuer(JwtProperties.ISS)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET_A));
    }

    /**
     * RefreshToken을 생성하는 메서드
     * 해싱 알고리즘(HMAC512)으로 jwt를 생성합니다.
     * @return JwtRefreshToken
     */
    public String generateRefreshToken(Long userId) {
            return JWT.create()
                    .withClaim("userId", userId)
                    .withExpiresAt(LocalDateTime.now().plusDays(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME).toInstant(ZoneOffset.of("+9")))
                    .withIssuer(JwtProperties.ISS)
                    .sign(Algorithm.HMAC512(JwtProperties.SECRET_B));
    }

    public String generateRefreshToken(Long userId, Duration duration){
        return JWT.create()
                .withClaim("userId", userId)
                .withExpiresAt(LocalDateTime.now().plus(duration).toInstant(ZoneOffset.of("+9")))
                .withIssuer(JwtProperties.ISS)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET_A));
    }

}
