package com.example.naejango.global.auth.handler;

import com.example.naejango.global.auth.jwt.JwtCookieHandler;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.repository.RefreshTokenRepository;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtGenerator jwtGenerator;
    private final AuthenticationHandler authenticationHandler;
    private final JwtCookieHandler jwtCookieHandler;
    private final String redirectUrl = "https://naejango.site/oauth/KakaoCallback";
    private final String localRedirectUrl = "https://localhost:3000/oauth/kakaoCallback";

    /**
     * OAuth 로그인 처리가 성공적으로 수행되어 Authentication 객체가 만들어 진 경우 실행되는 메서드.
     *  - 만약 RefreshToken 기존에 이미 있는 경우에는 해당 로그인 절차를 무시
     *  - RefreshToken 이 없는 경우에만 RefreshToken 을 발급
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        Optional<String> refreshToken = jwtCookieHandler.getRefreshToken(request);
        if (refreshToken.isPresent()){
            response.sendRedirect(localRedirectUrl + "?loginStatus=already_logged_in");
            return;
        }
        generateAndSetTokenCookies(authentication, response);
    }

    private void generateAndSetTokenCookies(Authentication authentication, HttpServletResponse response) throws IOException {
        String redirection = localRedirectUrl;

        String role = authenticationHandler.getRole(authentication).name();
        Long userId = authenticationHandler.getUserId(authentication);

        String accessToken = jwtGenerator.generateAccessToken(userId);
        String refreshToken = jwtGenerator.generateRefreshToken(userId);
        jwtCookieHandler.addAccessTokenCookie(accessToken, response);
        jwtCookieHandler.addRefreshTokenCookie(refreshToken, response);
        refreshTokenRepository.saveRefreshToken(userId, refreshToken);

        redirection += "?loginStatus=" + role;
        redirection += "&accessToken=" + accessToken; // 로컬 개발 환경

        response.sendRedirect(redirection);
    }
}