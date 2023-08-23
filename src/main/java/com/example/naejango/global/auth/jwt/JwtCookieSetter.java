package com.example.naejango.global.auth.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class JwtCookieSetter {

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

    public void deleteAccessTokenCookie(Cookie accessTokenCookie, HttpServletResponse response) {
        setAccessTokenCookie(accessTokenCookie);
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);
    }
    public void deleteRefreshTokenCookie(Cookie refreshTokenCookie, HttpServletResponse response) {
        setRefreshTokenCookie(refreshTokenCookie);
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
    }


}
