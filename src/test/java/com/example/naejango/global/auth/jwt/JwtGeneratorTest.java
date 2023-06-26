package com.example.naejango.global.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.naejango.domain.user.domain.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@Tag("jwt")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JwtGeneratorTest {

    @Autowired
    private JwtGenerator jwtGenerator;
    private String accessToken;
    private String refreshToken;
    private User testUser;
    @Test
    @DisplayName("JwtGenerator가 JWT를 생성")
    void generateJWT(){
        testUser = User.builder().userKey("test").build();
        accessToken = jwtGenerator.generateAccessToken(testUser);
        refreshToken = jwtGenerator.generateRefreshToken(testUser);
    }

    @Nested
    @DisplayName("생성된 JWT는")
    class WhenGeneratedJWT {
        @Test
        @DisplayName("null이 아님")
        void isNotNull() {
            assertNotNull(accessToken);
            assertNotNull(refreshToken);
        }

        @Test
        @DisplayName("token holder의 userKey를 담고있음")
        void decode(){
            DecodedJWT decodedAccessToken = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(accessToken);
            DecodedJWT decodedRefreshToken = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(refreshToken);

            assertEquals(testUser.getUserKey(), decodedAccessToken.getClaim("userKey").asString());
            assertEquals(testUser.getUserKey(), decodedRefreshToken.getClaim("userKey").asString());
        }
        @Test
        @DisplayName("유효기간이 정상")
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