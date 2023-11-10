package com.example.naejango.global.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.global.auth.repository.RefreshTokenRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtValidator {

    private final RefreshTokenRepository refreshTokenRepository;

    public Optional<JwtPayload> validateAccessToken(String accessToken) {
        if (accessToken == null) return Optional.empty();

        DecodedJWT decodedAccessToken = decodeAccessToken(accessToken);
        if (isExpiredToken(decodedAccessToken)) return Optional.empty();

        return Optional.ofNullable(getJwtPayload(decodedAccessToken));
    }

    public Optional<JwtPayload> validateRefreshToken(String refreshToken) {
        if (refreshToken == null) return Optional.empty();

        DecodedJWT decodedRefreshToken = decodeRefreshToken(refreshToken);
        if(isExpiredToken(decodedRefreshToken)) return Optional.empty();

        JwtPayload jwtPayload = getJwtPayload(decodedRefreshToken);
        String storedToken = refreshTokenRepository.getRefreshToken(jwtPayload.getUserId());
        if (!refreshToken.equals(storedToken)) return Optional.empty();

        return Optional.of(jwtPayload);
    }

    public Optional<JwtPayload> validateRefreshToken(HttpServletRequest request) {
        return validateAccessToken(getAccessToken(request));
    }

    public Optional<JwtPayload> validateRefreshToken(StompHeaderAccessor headerAccessor) {
        return validateAccessToken(getAccessToken(headerAccessor));
    }

    private String getAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(JwtProperties.ACCESS_TOKEN_HEADER);
        return getAccessToken(authorizationHeader);
    }

    private String getAccessToken(StompHeaderAccessor headerAccessor) {
        String authorizationHeader = headerAccessor.getFirstNativeHeader(JwtProperties.ACCESS_TOKEN_HEADER);
        return getAccessToken(authorizationHeader);
    }

    private String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(JwtProperties.ACCESS_TOKEN_PREFIX)) {
            return authorizationHeader.replace(JwtProperties.ACCESS_TOKEN_PREFIX, "");
        }
        return null;
    }

    private DecodedJWT decodeAccessToken(String accessToken){
        try {
            return JWT.require(Algorithm.HMAC512(JwtProperties.SECRET_A)).build().verify(accessToken);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            // Token 이 있으나 복호화 실패
            log.error(e.getMessage());
            throw new CustomException(ErrorCode.TOKEN_DECRYPTION_FAILURE);
        }
    }

    private DecodedJWT decodeRefreshToken(String refreshToken){
        try {
            return JWT.require(Algorithm.HMAC512(JwtProperties.SECRET_B)).build().verify(refreshToken);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            // Token 이 있으나 복호화 실패
            log.error(e.getMessage());
            throw new CustomException(ErrorCode.TOKEN_DECRYPTION_FAILURE);
        }
    }

    private boolean isExpiredToken(DecodedJWT decodedToken){
        Instant exp = decodedToken.getClaim("exp").asInstant();
        return exp.isBefore(Instant.now());
    }

    private JwtPayload getJwtPayload(DecodedJWT decodedAccessToken) {
        Long userId = decodedAccessToken.getClaim("userId").asLong();
        Role role = Role.valueOf(decodedAccessToken.getClaim("role").asString());
        return JwtPayload.builder().userId(userId).role(role).build();
    }
}