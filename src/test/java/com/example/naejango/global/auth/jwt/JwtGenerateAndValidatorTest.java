package com.example.naejango.global.auth.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.dto.ValidateTokenResponseDto;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyLong;

@ActiveProfiles("test")
@SpringBootTest
class JwtGenerateAndValidatorTest {

    @Autowired
    private JwtGenerator jwtGenerator;
    @Autowired
    private JwtValidator jwtValidator;
    @MockBean
    private UserRepository userRepositoryMock;

    @Test
    @DisplayName("generateToken: 토큰 생성 테스트")
    void generateJwt(){
        // given
        User testUser = User.builder().id(1L).userKey("test").build();

        // when
        String accessToken = jwtGenerator.generateAccessToken(testUser.getId());
        String refreshToken = jwtGenerator.generateRefreshToken(testUser.getId());

        // then
        assertNotNull(accessToken);
        assertNotNull(refreshToken);
    }

    @Test
    @Disabled("Github Actions Runner 환경에서 실패: 원인 분석중")
    @DisplayName("decodeJwt: 토큰 복호화 테스트")
    void decodeJwt(){
        // given
        User testUser = User.builder().id(1L).userKey("test").build();
        String accessToken = jwtGenerator.generateAccessToken(testUser.getId());
        String refreshToken = jwtGenerator.generateRefreshToken(testUser.getId());

        // when
        DecodedJWT decodedAccessToken = jwtValidator.decodeJwt(accessToken);
        DecodedJWT decodedRefreshToken = jwtValidator.decodeJwt(refreshToken);

        // then
        String accessTokenIss = decodedAccessToken.getIssuer();
        Long accessTokenUserId = decodedAccessToken.getClaim("userId").asLong();
        String refreshTokenIss = decodedAccessToken.getIssuer();
        Long refreshTokenUserId = decodedRefreshToken.getClaim("userId").asLong();

        assertEquals(1L, accessTokenUserId);
        assertEquals("TeamNaeJanGo", accessTokenIss);
        assertEquals(1L, refreshTokenUserId);
        assertEquals("TeamNaeJanGo", refreshTokenIss);
    }

    @Test
    @Disabled("Github Actions Runner 환경에서 실패: 원인 분석중")
    @DisplayName("validateAccessToken: 유효한 access 토큰 검증")
    public void validateAccessTokenTest() {
        // given
        User testUser = User.builder().id(1L).userKey("test").build();
        String accessToken = jwtGenerator.generateAccessToken(testUser.getId());

        // when
        ValidateTokenResponseDto result = jwtValidator.validateAccessToken(accessToken);

        // then
        assertTrue(result.isValidToken());
        assertEquals(1L, result.getUserId());
    }

    @Test
    @DisplayName("validateRefreshToken: 유효한 refresh 토큰 검증")
    public void validateRefreshTokenTest() {
        // given
        User testUser = User.builder().id(1L).userKey("test").build();
        String refreshToken = jwtGenerator.generateRefreshToken(testUser.getId());
        testUser.refreshSignature(jwtValidator.decodeJwt(refreshToken).getSignature());
        BDDMockito.given(userRepositoryMock.findById(anyLong())).willReturn(Optional.of(testUser));

        // when
        ValidateTokenResponseDto result = jwtValidator.validateRefreshToken(refreshToken);

        // then
        assertTrue(result.isValidToken());
        assertEquals(1L, result.getUserId());
    }


}