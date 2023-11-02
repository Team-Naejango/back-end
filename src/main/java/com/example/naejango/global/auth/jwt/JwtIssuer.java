package com.example.naejango.global.auth.jwt;

import com.example.naejango.global.auth.repository.RefreshTokenRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtIssuer {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtValidator jwtValidator;
    private final JwtGenerator jwtGenerator;
    private final JwtCookieHandler jwtCookieHandler;

    public void issueTokenCookie(JwtPayload jwtPayload, HttpServletResponse response) {
        // 토큰 생성
        String accessToken = jwtGenerator.generateAccessToken(jwtPayload);
        String refreshToken = jwtGenerator.generateRefreshToken(jwtPayload);

        // 쿠키 발급
        jwtCookieHandler.addAccessTokenCookie(accessToken, response);
        jwtCookieHandler.addRefreshTokenCookie(refreshToken, response);

        // 발급한 리프레시 토큰 저장
        refreshTokenRepository.saveRefreshToken(jwtPayload.getUserId(), refreshToken);
    }

    public Optional<String> reissueAccessToken(HttpServletRequest request) {
        Optional<String> refreshTokenOpt = jwtCookieHandler.getRefreshToken(request);

        // 리프레시 토큰이 없는 경우
        if(refreshTokenOpt.isEmpty()) return Optional.empty();
        log.info("check : 2");
        // 리프레시 토큰 검증
        JwtPayload jwtPayload = jwtValidator.validateRefreshToken(refreshTokenOpt.get())
                .orElseThrow(() -> new CustomException(ErrorCode.REISSUE_TOKEN_FAILURE));
        log.info("check : 7");
        return Optional.ofNullable(jwtGenerator.generateAccessToken(jwtPayload));
    }

}
