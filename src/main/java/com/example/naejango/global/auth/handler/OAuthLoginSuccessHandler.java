package com.example.naejango.global.auth.handler;

import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
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
    private final String redirectUrl = "http://localhost:3000/oauth/kakaoCallback";
    private final String localFrontDomain = "http://localhost:3000";
    private final String FrontDomain = "https://d1ad0vl3i2dudp.cloudfront.net";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        System.out.println("login");
        Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("RefreshToken"))
                .findAny()
                .ifPresentOrElse(
                        cookie -> {},
                        () -> generateJWT(authentication, response)
                );

        response.sendRedirect(redirectUrl);
    }

    private void generateJWT(Authentication authentication, HttpServletResponse response) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        String accessToken = jwtGenerator.generateAccessToken(userId);
        String refreshToken = jwtGenerator.generateRefreshToken(userId);
        userService.refreshSignature(userId, refreshToken);

        Cookie accessTokenCookie = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, accessToken);
        Cookie refreshTokenCookie1 = new Cookie(JwtProperties.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        Cookie refreshTokenCookie2 = new Cookie(JwtProperties.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        accessTokenCookie.setHttpOnly(false); // 자바스크립트 접근 허용
        refreshTokenCookie1.setHttpOnly(true);
        refreshTokenCookie1.setPath(localFrontDomain);
        refreshTokenCookie2.setHttpOnly(true);
        refreshTokenCookie2.setPath(FrontDomain);
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie1);
        response.addCookie(refreshTokenCookie2);
    }
}
