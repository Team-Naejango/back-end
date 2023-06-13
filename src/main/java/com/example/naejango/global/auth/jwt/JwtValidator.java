package com.example.naejango.global.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.naejango.domain.user.entity.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.dto.TokenValidateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtValidator {
    private final UserRepository userRepository;
    private final JwtGenerator jwtGenerator;
    /**
     * access token 의 유효성을 검증
     * access token payload의 "exp"(유효시간)을
     * 현재 시간과 비교하여 더 늦다면 false 반환
     */

    public TokenValidateResponse validateToken(HttpServletRequest request){
        TokenValidateResponse validateResponse = new TokenValidateResponse();

        // AccessToken 의 유효성 검증
        String accessToken = getAccessToken(request);
        if(accessToken!=null) {
            DecodedJWT decodedAccessToken = decodeJwt(accessToken);
            if (isExpiredToken(decodedAccessToken)) {
                validateResponse.setValidAccessToken(false);
            } else {
                validateResponse.setValidAccessToken(true);
                validateResponse.setUserKey(decodedAccessToken.getClaim("userKey").asString());
            }
        } else {
            validateResponse.setValidAccessToken(false);
        }

        // RefreshToken 의 유효성 검증
        String refreshToken = getRefreshToken(request);
        if (refreshToken != null) {
            DecodedJWT decodedRefreshToken = decodeJwt(refreshToken);
            if(isExpiredToken(decodedRefreshToken)) {
                validateResponse.setValidRefreshToken(false);
            } else {
                if (isVerifiedSignature(decodedRefreshToken)) {
                    validateResponse.setValidRefreshToken(true);
                    validateResponse.setUserKey(decodedRefreshToken.getClaim("userKey").asString());
                } else {
                    validateResponse.setValidRefreshToken(false);
                }
            }
        } else {
            validateResponse.setValidRefreshToken(false);
        }

        if(!validateResponse.isValidAccessToken() && validateResponse.isValidRefreshToken()){
            User user = userRepository.findByUserKey(validateResponse.getUserKey());
            String reIssuedAccessToken = jwtGenerator.generateAccessToken(user);
            validateResponse.setReIssuedAccessToken(reIssuedAccessToken);
        }

        return validateResponse;
    }

    private DecodedJWT decodeJwt(String token){
        try {
            return JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(token);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        // Token 정보가 확인 되나, 디코딩이 되지 않는 exception
    }

    /**
     * request로 부터 access Token을 가져옴
     *
     * @param request : Http 요청
     * @return accessToken
     */
    public String getAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith(JwtProperties.ACCESS_TOKEN_PREFIX)) {
            return null;
        }
        return authorizationHeader.replace(JwtProperties.ACCESS_TOKEN_PREFIX, "");
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

    private boolean isExpiredToken(DecodedJWT decodedToken){
        Instant exp = decodedToken.getClaim("exp").asInstant();
        return exp.isBefore(Instant.now());
    }

    private boolean isVerifiedSignature(DecodedJWT decodedJWT) {
        String userKey;
        try {
            userKey = decodedJWT.getClaim("userKey").asString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        User user = userRepository.findByUserKey(userKey);
        return user.getSignature().equals(decodedJWT.getSignature());
    }


}