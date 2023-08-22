package com.example.naejango.global.auth.handler;

import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.global.auth.jwt.JwtCookieSetter;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserService userService;
    private final JwtGenerator jwtGenerator;
    private final CommonDtoHandler commonDtoHandler;
    private final JwtCookieSetter jwtCookieSetter;
    private final String redirectUrl = "https://naejango.site/oauth/KakaoCallback";
    private final String localRedirectUrl = "http://localhost:3000/oauth/kakaoCallback";

    /**
     * OAuth 로그인 처리가 성공적으로 수행되어 Authentication 객체가 만들어 진 경우 실행되는 메서드.
     *  - 만약 RefreshToken 기존에 이미 있는 경우에는 해당 로그인 절차를 무시
     *  - RefreshToken 이 없는 경우에만 RefreshToken 을 발급
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("RefreshToken"))
                .findAny()
                .ifPresentOrElse(
                        cookie -> {
                            try {
                                response.sendRedirect(localRedirectUrl);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        () -> {
                            try {
                                generateAndSetTokenCookies(authentication, response);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );


    }

    private void generateAndSetTokenCookies(Authentication authentication, HttpServletResponse response) throws IOException {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);

        String accessToken = jwtGenerator.generateAccessToken(userId);
        String refreshToken = jwtGenerator.generateRefreshToken(userId);
        userService.refreshSignature(userId, refreshToken);

        jwtCookieSetter.addAccessTokenCookie(accessToken, response);
        jwtCookieSetter.addRefreshTokenCookie(refreshToken, response);

        response.sendRedirect(localRedirectUrl + "?accesstoken=" + accessToken);
    }
}