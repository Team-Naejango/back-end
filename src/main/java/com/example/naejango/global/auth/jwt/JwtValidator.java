package com.example.naejango.global.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.naejango.domain.user.entity.User;
import com.example.naejango.global.auth.dto.TokenValidateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtValidator {
    public TokenValidateResponse validateAccessToken(HttpServletRequest request) {
        TokenValidateResponse validateResponse = new TokenValidateResponse();

        // AccessToken 의 유효성 검증
        String accessToken = getAccessToken(request);
        if (accessToken != null) {
            DecodedJWT decodedAccessToken = decodeJwt(accessToken);
            if (isExpiredToken(decodedAccessToken)) {
                validateResponse.setValidToken(false);
            } else {
                validateResponse.setValidToken(true);
                validateResponse.setUserKey(decodedAccessToken.getClaim("userKey").asString());
            }
        } else {
            validateResponse.setValidToken(false);
        }
        return validateResponse;
    }

    public TokenValidateResponse validateRefreshToken(HttpServletRequest request, User user) {
        TokenValidateResponse validateResponse = new TokenValidateResponse();

        String refreshToken = getRefreshToken(request);
        if (refreshToken != null) {
            DecodedJWT decodedRefreshToken = decodeJwt(refreshToken);
            if(isExpiredToken(decodedRefreshToken)) {
                validateResponse.setValidToken(false);
            } else {
                if (isVerifiedSignature(decodedRefreshToken, user)) {
                    validateResponse.setValidToken(true);
                    validateResponse.setUserKey(decodedRefreshToken.getClaim("userKey").asString());
                } else {
                    validateResponse.setValidToken(false);
                }
            }
        } else {
            validateResponse.setValidToken(false);
        }

        return validateResponse;
    }

    public DecodedJWT decodeJwt(String token){
        try {
            return JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(token);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(JwtProperties.ACCESS_TOKEN_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith(JwtProperties.ACCESS_TOKEN_PREFIX)) {
            return null;
        }
        return authorizationHeader.replace(JwtProperties.ACCESS_TOKEN_PREFIX, "");
    }

    public String getUserKey(HttpServletRequest request){
        String refreshToken = getRefreshToken(request);
        if (refreshToken == null) {
            return null;
        }
        String userKey;
        try {
            userKey = decodeJwt(refreshToken).getClaim("userKey").asString();
        } catch (Exception e) {
            throw new RuntimeException("Fail to decode refresh token");
        }
        return userKey;
    }

    /**
     * request로 부터 refresh Token을 가져옴
     *
     * @param request : Http 요청
     * @return refreshToken
     */
    public String getRefreshToken(HttpServletRequest request) {
        String refreshTokenCookie = null;
        Cookie[] cookies = request.getCookies();
        if(cookies==null) return null;
        for (Cookie cookie : cookies) {
            if (cookie!=null && cookie.getName().equals(JwtProperties.REFRESH_TOKEN_HEADER)) {
                refreshTokenCookie = cookie.getValue();
            }
        }
        if (refreshTokenCookie==null || !refreshTokenCookie.startsWith(JwtProperties.REFRESH_TOKEN_PREFIX)) {
            return null;
        }
        return refreshTokenCookie.replace(JwtProperties.REFRESH_TOKEN_PREFIX, "");
    }

    public boolean isExpiredToken(DecodedJWT decodedToken){
        Instant exp = decodedToken.getClaim("exp").asInstant();
        return exp.isBefore(Instant.now());
    }

    public boolean isVerifiedSignature(DecodedJWT decodedJWT, User user) {
        return user.getSignature().equals(decodedJWT.getSignature());
    }


}