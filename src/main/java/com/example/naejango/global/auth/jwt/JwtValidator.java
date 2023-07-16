package com.example.naejango.global.auth.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.dto.ValidateTokenResponseDto;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final UserRepository userRepository;

    public ValidateTokenResponseDto validateAccessToken(String accessToken) {
        ValidateTokenResponseDto validateResponse = new ValidateTokenResponseDto();

        // AccessToken 의 유효성 검증
        if (accessToken != null) {
            DecodedJWT decodedAccessToken = decodeJwt(accessToken);
            if (isExpiredToken(decodedAccessToken)) {
                validateResponse.setValidToken(false);
            } else {
                validateResponse.setValidToken(true);
                validateResponse.setUserId(decodedAccessToken.getClaim("userId").asLong());
            }
        } else {
            validateResponse.setValidToken(false);
        }
        return validateResponse;
    }

    public ValidateTokenResponseDto validateRefreshToken(String refreshToken) {
        ValidateTokenResponseDto validateResponse = new ValidateTokenResponseDto();

        if (refreshToken != null) {
            DecodedJWT decodedRefreshToken = decodeJwt(refreshToken);
            if(isExpiredToken(decodedRefreshToken)) {
                validateResponse.setValidToken(false);
            } else {
                Long userId = decodedRefreshToken.getClaim("userId").asLong();
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                if (isVerifiedSignature(decodedRefreshToken, user)) {
                    validateResponse.setValidToken(true);
                    validateResponse.setUserId(userId);
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
            // Token이 있으나 복호화 실패
            throw new RuntimeException(e);
        }
    }

    public boolean isExpiredToken(DecodedJWT decodedToken){
        Instant exp = decodedToken.getClaim("exp").asInstant();
        return exp.isBefore(Instant.now());
    }

    public boolean isVerifiedSignature(DecodedJWT decodedJWT, User user) {
        return user.getSignature().equals(decodedJWT.getSignature());
    }

}