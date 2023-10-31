package com.example.naejango.global.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.example.naejango.global.auth.jwt.JwtProperties.*;

@Component
@RequiredArgsConstructor
public class JwtGenerator {
    /**
     * AccessToken을 생성하는 메서드
     * 해싱 알고리즘(HMAC512)으로 jwt를 생성합니다.
     * @return JwtAccessToken
     */
    public String generateAccessToken(JwtPayload jwtPayload, Duration duration){
        return JWT.create()
                .withClaim("userId", jwtPayload.getUserId())
                .withClaim("role", jwtPayload.getRole().name())
                .withExpiresAt(LocalDateTime.now().plus(duration).toInstant(ZoneOffset.of("+9")))
                .withIssuer(ISS)
                .sign(Algorithm.HMAC512(SECRET_A));
    }

    public String generateAccessToken(JwtPayload jwtPayload) {
        return generateAccessToken(jwtPayload, Duration.ofMinutes(ACCESS_TOKEN_EXPIRATION_TIME));
    }

    /**
     * RefreshToken을 생성하는 메서드
     * 해싱 알고리즘(HMAC512)으로 jwt를 생성합니다.
     * @return JwtRefreshToken
     */
    public String generateRefreshToken(JwtPayload jwtPayload, Duration duration){
        return JWT.create()
                .withClaim("userId", jwtPayload.getUserId())
                .withClaim("role", jwtPayload.getRole().name())
                .withExpiresAt(LocalDateTime.now().plus(duration).toInstant(ZoneOffset.of("+9")))
                .withIssuer(ISS)
                .sign(Algorithm.HMAC512(SECRET_B));
    }

    public String generateRefreshToken(JwtPayload jwtPayload) {
        return generateRefreshToken(jwtPayload, Duration.ofDays(REFRESH_TOKEN_EXPIRATION_TIME));
    }



}
