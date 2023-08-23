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

    private void setAccessTokenCookie(Cookie accessTokenCookie) {
        accessTokenCookie.setHttpOnly(false);
//        accessTokenCookie.setSecure(true);
//        accessTokenCookie.setDomain("naejango.site");
        accessTokenCookie.setPath("/");
    }

    private void setRefreshTokenCookie(Cookie refreshTokenCookie) {
        refreshTokenCookie.setHttpOnly(true);
//        refreshTokenCookie.setSecure(true);
//        refreshTokenCookie.setDomain("naejango.site");
        refreshTokenCookie.setPath("/");
    }

    public void addAccessTokenCookie(String accessToken, HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, accessToken);
        setAccessTokenCookie(accessTokenCookie);
        response.addCookie(accessTokenCookie);
    }

    public void addRefreshTokenCookie(String refreshToken, HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie(JwtProperties.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        setRefreshTokenCookie(refreshTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    public void deleteRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) return;

        Optional<Cookie> refreshTokenCookieOpt = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(JwtProperties.REFRESH_TOKEN_COOKIE_NAME))
                .findAny();
        if(refreshTokenCookieOpt.isEmpty()) return;

        Cookie refreshTokenCookie = refreshTokenCookieOpt.get();
        setRefreshTokenCookie(refreshTokenCookie);
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
    }
    public void deleteAccessTokenCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) return;
        Optional<Cookie> accessTokenCookieOpt = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(JwtProperties.ACCESS_TOKEN_COOKIE_NAME))
                .findAny();
        if(accessTokenCookieOpt.isEmpty()) return;
        Cookie accessTokenCookie = accessTokenCookieOpt.get();
        setRefreshTokenCookie(accessTokenCookie);
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);
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
