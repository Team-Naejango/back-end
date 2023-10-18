package com.example.naejango.global.auth.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.naejango.domain.account.domain.Account;
import com.example.naejango.domain.account.repository.AccountRepository;
import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.auth.repository.RefreshTokenRepository;
import com.example.naejango.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.example.naejango.global.auth.jwt.JwtProperties.*;

@SpringBootTest
@ActiveProfiles({"Test"})
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
public class JwtAuthTest {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired UserProfileRepository userProfileRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired StringRedisTemplate redisTemplate;
    @Autowired EntityManager em;
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
        testUser = User.builder().role(Role.USER).userKey("test_1").password("").build();
        UserProfile userProfile = UserProfile.builder().nickname("김씨").imgUrl("imgUrl").intro("테스트 유저 1 입니다.").
                birth("19900000").gender(Gender.MALE).phoneNumber("01012345678").build();
        Account account = Account.builder().user(testUser).build();
        userRepository.save(testUser);
        userProfileRepository.save(userProfile);
        accountRepository.save(account);
        testUser.setUserProfile(userProfile);

        accessTokenNormal = jwtGenerator.generateAccessToken(testUser.getId());
        refreshTokenNormal = jwtGenerator.generateRefreshToken(testUser.getId());
        refreshTokenRepository.saveRefreshToken(testUser.getId(), refreshTokenNormal);
        refreshTokenNotRegistered = JWT.create()
                .withClaim("userId", testUser.getId())
                .withExpiresAt(LocalDateTime.now().plusDays(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME + 1).toInstant(ZoneOffset.of("+9")))
                .withIssuer(JwtProperties.ISS)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET_B));

        accessTokenInvalid = "fake";
        refreshTokenInvalid = "fake";

        accessTokenUserNotFound = jwtGenerator.generateAccessToken(90384769835L);
        accessTokenUserNotFound = jwtGenerator.generateAccessToken(90384769835L);
    }
    @Nested
    @DisplayName("RefreshToken 이 없는 경우")
    class NoRefreshToken {

        @Test
        @DisplayName("accessToken 이 없는 경우 : CustomAuthenticationEntryPoint 진입")
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
        @DisplayName("accessToken 이 유효하지 않은 경우 : JwtAuthenticationFilter 진입, ExceptionHandlingFilter 핸들링")
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
        @DisplayName("accessToken 의 User 를 인식하지 못하는 경우 : JwtAuthenticationFilter 진입, ExceptionHandlingFilter 핸들링")
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
    @DisplayName("AccessToken 이 없고, RefreshToken 은 있는 경우")
    class RefreshToken {

        @Test
        @DisplayName("유효한 RefreshToken 이 있는 경우 : 토큰 재발급")
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
