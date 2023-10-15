package com.example.naejango.global.auth.jwt;

import com.example.naejango.global.auth.repository.RefreshTokenRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtIssuer {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtValidator jwtValidator;
    private final JwtGenerator jwtGenerator;
    private final JwtCookieHandler jwtCookieHandler;

    public void issueTokenCookie(Long userId, HttpServletResponse response) {
        // 토큰 생성
        String accessToken = jwtGenerator.generateAccessToken(userId);
        String refreshToken = jwtGenerator.generateRefreshToken(userId);

        // 쿠키 발급
        jwtCookieHandler.addAccessTokenCookie(accessToken, response);
        jwtCookieHandler.addRefreshTokenCookie(refreshToken, response);

        // 발급한 리프레시 토큰 저장
        refreshTokenRepository.saveRefreshToken(userId, refreshToken);
    }

    public Optional<String> reissueAccessToken(HttpServletRequest request) {
        Optional<String> refreshTokenOpt = jwtCookieHandler.getRefreshToken(request);
        // 리프레시 토큰이 없는 경우
        if(refreshTokenOpt.isEmpty()) return Optional.empty();
        // 리프레시 토큰 검증
        Long userId = jwtValidator.validateRefreshToken(refreshTokenOpt.get())
                .orElseThrow(() -> new CustomException(ErrorCode.REISSUE_TOKEN_FAILURE));
        return Optional.ofNullable(jwtGenerator.generateAccessToken(userId));
    }

}
