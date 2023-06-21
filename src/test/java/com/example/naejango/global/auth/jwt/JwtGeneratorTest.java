package com.example.naejango.global.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.naejango.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("JwtGenerator가")
class JwtGeneratorTest {
    @Autowired
    private JwtGenerator jwtGenerator;
    private String accessToken;
    private String refreshToken;
    private User testUser;
    @Test
    @DisplayName("JWT를 생성")
    void generateJWT(){
        testUser = User.builder().userKey("test").build();
        accessToken = jwtGenerator.generateAccessToken(testUser);
        refreshToken = jwtGenerator.generateRefreshToken(testUser);

        System.out.println("accessToken = " + accessToken);
        System.out.println("refreshToken = " + refreshToken);
    }

    @Nested
    @DisplayName("생성된 JWT는")
    class WhenGeneratedJWT {
        @Test
        @DisplayName("Null이 아님")
        void isNotNull() {
            System.out.println("accessToken = " + accessToken);
            System.out.println("refreshToken = " + refreshToken);
            assertNotNull(accessToken);
            assertNotNull(refreshToken);
        }

        @Test
        @DisplayName("Prefix가 정상임")
        void isProperPrefix() {
            assertTrue(accessToken.startsWith(JwtProperties.ACCESS_TOKEN_PREFIX));
            assertTrue(refreshToken.startsWith(JwtProperties.REFRESH_TOKEN_PREFIX));
        }

        @Test
        @DisplayName("token holder의 userKey를 담고있음")
        void decode(){
            DecodedJWT decodedAccessToken = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(accessToken.replace(JwtProperties.ACCESS_TOKEN_PREFIX, ""));
            DecodedJWT decodedRefreshToken = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(refreshToken.replace(JwtProperties.REFRESH_TOKEN_PREFIX, ""));

            assertEquals(testUser.getUserKey(), decodedAccessToken.getClaim("userKey").asString());
            assertEquals(testUser.getUserKey(), decodedRefreshToken.getClaim("userKey").asString());
        }
        @Test
        @DisplayName("유효기간이 정상임")
        void expiration(){
            DecodedJWT decodedAccessToken = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(accessToken.replace(JwtProperties.ACCESS_TOKEN_PREFIX, ""));
            DecodedJWT decodedRefreshToken = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(refreshToken.replace(JwtProperties.REFRESH_TOKEN_PREFIX, ""));

            long accessTokenEpoch = decodedAccessToken.getClaim("exp").asInstant().getEpochSecond();
            long refreshTokenEpoch = decodedRefreshToken.getClaim("exp").asInstant().getEpochSecond();
            long now = Instant.now().getEpochSecond();

            long accessTokenExp = (accessTokenEpoch - now) / 60;
            long refreshTokenExp = (refreshTokenEpoch - now) / (60 * 60 * 24);

            assertEquals(accessTokenExp, 30);
            assertEquals(refreshTokenExp, 14);
        }


    }
}