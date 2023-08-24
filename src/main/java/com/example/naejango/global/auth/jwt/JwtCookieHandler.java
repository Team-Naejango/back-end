package com.example.naejango.global.auth.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtCookieHandler {

    private String setAccessTokenCookie(String accessToken) {
        return "AccessToken=" + accessToken + ";   " +
                "Secure;   " +
                "Path=/;   " +
                "SameSite=None";
    }

    private String setRefreshTokenCookie(String refreshToken) {
        return "RefreshToken=" + refreshToken + ";   " +
                "Secure;   " +
                "Path=/;   " +
                "SameSite=None;   " +
                "HttpOnly   ";
    }

    public void addAccessTokenCookie(String accessToken, HttpServletResponse response) {
        String accessTokenCookieHeader = setAccessTokenCookie(accessToken);
        response.setHeader("Set-Cookie", accessTokenCookieHeader);
    }

    public void addRefreshTokenCookie(String refreshToken, HttpServletResponse response) {
        String refreshTokenCookieHeader = setRefreshTokenCookie(refreshToken);
        response.setHeader("Set-Cookie", refreshTokenCookieHeader);
    }

    public void deleteRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) return;

        Optional<Cookie> refreshTokenCookieOpt = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(JwtProperties.REFRESH_TOKEN_COOKIE_NAME))
                .findAny();
        if(refreshTokenCookieOpt.isEmpty()) return;

        Cookie refreshTokenCookie = refreshTokenCookieOpt.get();
        String refreshToken = refreshTokenCookie.getValue();
        String refreshTokenCookieHeader= setRefreshTokenCookie(refreshToken);
        response.setHeader("Set-Cookie", refreshTokenCookieHeader+";   Max-Age=0");
    }
    public void deleteAccessTokenCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        if(cookies == null) return;
        Optional<Cookie> accessTokenCookieOpt = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(JwtProperties.ACCESS_TOKEN_COOKIE_NAME))
                .findAny();

        if(accessTokenCookieOpt.isEmpty()) return;

        Cookie accessTokenCookie = accessTokenCookieOpt.get();
        String accessToken = accessTokenCookie.getValue();
        String accessTokenCookieHeader = setAccessTokenCookie(accessToken);
        response.setHeader("Set-Cookie", accessTokenCookieHeader+";   Max-Age=0");
    }
    public boolean checkRefreshCookieDuplication(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        return cookies != null && Arrays.stream(cookies)
                .anyMatch(cookie -> cookie.getName().equals(JwtProperties.REFRESH_TOKEN_COOKIE_NAME));
    }

    public boolean checkAccessCookieDuplication(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        return cookies != null && Arrays.stream(cookies)
                .anyMatch(cookie -> cookie.getName().equals(JwtProperties.ACCESS_TOKEN_COOKIE_NAME));
    }

}
