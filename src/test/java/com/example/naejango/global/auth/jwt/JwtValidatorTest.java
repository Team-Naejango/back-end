//package com.example.naejango.global.auth.jwt;
//
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.interfaces.DecodedJWT;
//import com.example.naejango.domain.user.domain.User;
//import com.example.naejango.global.auth.dto.TokenValidateResponse;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@Tag("jwt")
//@SpringBootTest
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class JwtValidatorTest {
//
//    @Autowired
//    private JwtValidator jwtValidator;
//
//    private static User user;
//    private static String accessToken;
//    private static String refreshToken;
//
//    private TokenValidateResponse accessTokenValidateResponse;
//    private TokenValidateResponse refreshTokenValidateResponse;
//
//    private DecodedJWT decodedAccessToken;
//    private DecodedJWT decodedRefreshToken;
//
//
//    @BeforeAll
//    static void setup() {
//        user = User.builder().userKey("test").build();
//        accessToken = JWT.create()
//                .withClaim("userKey", user.getUserKey())
//                .withExpiresAt(LocalDateTime.now().plusMinutes(JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME).toInstant(ZoneOffset.of("+9")))
//                .withIssuer(JwtProperties.ISS)
//                .sign(Algorithm.HMAC512(JwtProperties.SECRET));
//
//        refreshToken = JWT.create()
//                .withClaim("userKey", user.getUserKey())
//                .withExpiresAt(LocalDateTime.now().plusDays(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME).toInstant(ZoneOffset.of("+9")))
//                .withIssuer(JwtProperties.ISS)
//                .sign(Algorithm.HMAC512(JwtProperties.SECRET));
//
//        user.setSignature(JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(refreshToken).getSignature());
//    }
//
//    @Test
//    @DisplayName("JwtValidator가 decode 수행")
//    void decodeJwt(){
//        decodedAccessToken = jwtValidator.decodeJwt(accessToken);
//        decodedRefreshToken = jwtValidator.decodeJwt(refreshToken);
//    }
//
//    @Nested
//    @DisplayName("decode 결과는")
//    class DecodeResult {
//        @Test
//        @DisplayName("null이 아님")
//        void isNotNull(){
//            assertNotNull(decodedAccessToken);
//            assertNotNull(decodedRefreshToken);
//        }
//
//        @Test
//        @DisplayName("올바른 claim을 포함하고 있음")
//        void containClaims(){
//            String accessTokenIss = decodedAccessToken.getClaim("iss").asString();
//            String accessTokenUserKey = decodedAccessToken.getClaim("userKey").asString();
//
//            String refreshTokenIss = decodedRefreshToken.getClaim("iss").asString();
//            String refreshTokenUserKey= decodedRefreshToken.getClaim("userKey").asString();
//
//            assertEquals(accessTokenUserKey, "test");
//            assertEquals(accessTokenIss, JwtProperties.ISS);
//
//            assertEquals(refreshTokenUserKey, "test");
//            assertEquals(refreshTokenIss, JwtProperties.ISS);
//        }
//    }
//
//    @Test
//    @DisplayName("JwtValidator가 validate 수행")
//    void validationTest() {
//        accessTokenValidateResponse = jwtValidator.validateAccessToken(accessToken);
//        refreshTokenValidateResponse = jwtValidator.validateRefreshToken(refreshToken, user);
//    }
//
//    @Nested
//    @DisplayName("validate 결과는")
//    class ValidationResult {
//        @Test
//        @DisplayName("null이 아님")
//        void isNotNull(){
//            assertNotNull(accessTokenValidateResponse);
//            assertNotNull(refreshTokenValidateResponse);
//        }
//
//        @Test
//        @DisplayName("isValidToken이 true임")
//        void isValidToken(){
//            assertTrue(accessTokenValidateResponse.isValidToken());
//            assertTrue(refreshTokenValidateResponse.isValidToken());
//        }
//    }
//}