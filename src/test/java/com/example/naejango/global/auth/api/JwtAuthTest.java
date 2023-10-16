package com.example.naejango.global.auth.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.auth.repository.RefreshTokenRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.example.naejango.global.auth.jwt.JwtProperties.*;

@SpringBootTest
@ActiveProfiles("Test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
public class JwtAuthTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    JwtGenerator jwtGenerator;
    User testUser;
    String accessTokenNormal;
    String refreshTokenNormal;
    String accessTokenInvalid;
    String refreshTokenInvalid;
    String accessTokenUserNotFound;
    String refreshTokenNotRegistered;

    @BeforeEach
    void setup(){
        testUser = userRepository.findByUserKey("test_1").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        accessTokenNormal = jwtGenerator.generateAccessToken(testUser.getId());
        refreshTokenNormal = jwtGenerator.generateRefreshToken(testUser.getId());
        refreshTokenRepository.saveRefreshToken(testUser.getId(), refreshTokenNormal);
        refreshTokenNotRegistered = JWT.create()
                .withClaim("userId", testUser.getId())
                .withExpiresAt(LocalDateTime.now().plusDays(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME + 1).toInstant(ZoneOffset.of("+9")))
                .withIssuer(JwtProperties.ISS)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        accessTokenInvalid = "fake";
        refreshTokenInvalid = "fake";

        accessTokenUserNotFound = jwtGenerator.generateAccessToken(90384769835L);
        accessTokenUserNotFound = jwtGenerator.generateAccessToken(90384769835L);


    }

    @Nested
    @DisplayName("RefreshToken 이 없는 경우")
    class NoRefreshToken {

        @Test
        @DisplayName("accessToken 이 없는 경우 : CustomAuthenticationEntryPoint 발생")
        void test1() throws Exception {
            // given
            ResultActions resultActions = mockMvc
                    .perform(MockMvcRequestBuilders.get("/api/user/profile"));

            // then
            resultActions
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.NOT_LOGGED_IN.getMessage()));
        }

        @Test
        @DisplayName("accessToken 이 유효하지 않은 경우 : JwtAuthenticationFilter 발생, ExceptionHandlingFilter 핸들링")
        void test2() throws Exception {
            // given
            ResultActions resultActions = mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/api/user/profile")
                            .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN_PREFIX + accessTokenInvalid)
                    );

            // then
            resultActions
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.TOKEN_DECRYPTION_FAILURE.getMessage()));
        }

        @Test
        @DisplayName("accessToken 의 User 를 인식하지 못하는 경우 : JwtAuthenticationFilter 발생, ExceptionHandlingFilter 핸들링")
        void test3() throws Exception {
            // given
            ResultActions resultActions = mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/api/user/profile")
                            .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN_PREFIX + accessTokenUserNotFound)
                    );

            // then
            resultActions
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
        }

    }

    @Nested
    @DisplayName("AccessToken 이 없는 경우")
    class RefreshToken {

        @Test
        @DisplayName("유효한 RefreshToken 이 있는 경우")
        void test1() throws Exception {
            // given
            ResultActions resultActions = mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/api/user/profile")
                            .cookie(generateRefreshTokenCookie(refreshTokenNormal))
                    );

            // then
            resultActions
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.ACCESS_TOKEN_REISSUE.getMessage()));
        }

        @Test
        @DisplayName("유효하지 않은 RefreshToken 이 있는 경우 (복호화 실패)")
        void test2() throws Exception {
            // given
            ResultActions resultActions = mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/api/user/profile")
                            .cookie(generateRefreshTokenCookie(refreshTokenInvalid))
                    );

            // then
            resultActions
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.TOKEN_DECRYPTION_FAILURE.getMessage()));
        }

        @Test
        @DisplayName("유효하지 않은 RefreshToken 이 있는 경우 (등록되지 않은 Token)")
        void test3() throws Exception {
            // given
            ResultActions resultActions = mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/api/user/profile")
                            .cookie(generateRefreshTokenCookie(refreshTokenNotRegistered))
                    );

            // then
            resultActions
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(MockMvcResultMatchers.jsonPath("message").value(ErrorCode.REISSUE_TOKEN_FAILURE.getMessage()));
        }

    }

    @Nested
    @DisplayName("AccessToken 이 있는 경우 (정상 응답)")
    class Normal {
        @Test
        @DisplayName("정상 AccessToken")
        void test3() throws Exception {
            // given
            ResultActions resultActions = mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/api/user/profile")
                            .header(ACCESS_TOKEN_HEADER, ACCESS_TOKEN_PREFIX + accessTokenNormal)
                    );

            // then
            resultActions
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }
    }
    private Cookie generateRefreshTokenCookie(String refreshToken) {
        return new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
    }

}
