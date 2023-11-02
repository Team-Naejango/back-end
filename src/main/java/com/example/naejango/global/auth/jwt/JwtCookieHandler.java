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
    public Optional<String> getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(JwtProperties.REFRESH_TOKEN_COOKIE_NAME))
        .map(Cookie::getValue)
        .findAny();
    }

    public void addAccessTokenCookie(String accessToken, HttpServletResponse response) {
        String accessTokenCookieHeader = generateAccessTokenCookie(accessToken);
        response.setHeader("Set-Cookie", accessTokenCookieHeader);
    }

    public void addRefreshTokenCookie(String refreshToken, HttpServletResponse response) {
        String refreshTokenCookieHeader = generateRefreshTokenCookie(refreshToken);
        response.setHeader("Set-Cookie", refreshTokenCookieHeader);
    }

    public void deleteAllTokenCookie(HttpServletRequest request, HttpServletResponse response){
        deleteAccessTokenCookie(request, response);
        deleteRefreshTokenCookie(request, response);
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
        String refreshTokenCookieHeader= generateRefreshTokenCookie(refreshToken);
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
        String accessTokenCookieHeader = generateAccessTokenCookie(accessToken);
        response.setHeader("Set-Cookie", accessTokenCookieHeader+";   Max-Age=0");
    }

    public boolean hasRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        return cookies != null && Arrays.stream(cookies)
                .anyMatch(cookie -> cookie.getName().equals(JwtProperties.REFRESH_TOKEN_COOKIE_NAME));
    }



    private String generateAccessTokenCookie(String accessToken) {
        return "AccessToken=" + accessToken + ";   " +
                "Secure;   " +
                "Path=/;   " +
                "SameSite=None";
    }

    private String generateRefreshTokenCookie(String refreshToken) {
        return "RefreshToken=" + refreshToken + ";   " +
                "Secure;   " +
                "Path=/;   " +
                "HttpOnly   " +
                "SameSite=None";
    }

}
