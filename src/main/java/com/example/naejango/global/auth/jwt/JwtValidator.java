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
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final UserRepository userRepository;

    public ValidateTokenResponseDto validateTokenInRequest(HttpServletRequest request) {
        return validateAccessToken(getAccessToken(request));
    }

    public ValidateTokenResponseDto validateTokenInRequest(StompHeaderAccessor headerAccessor) {
        return validateAccessToken(getAccessToken(headerAccessor));
    }

    public ValidateTokenResponseDto validateAccessToken(String accessToken) {
        if (accessToken == null) {
            return new ValidateTokenResponseDto(null, false);
        }
        DecodedJWT decodedAccessToken = decodeJwt(accessToken);
        if (isExpiredToken(decodedAccessToken)) {
            return new ValidateTokenResponseDto(null, false);
        }
        return new ValidateTokenResponseDto(decodedAccessToken.getClaim("userId").asLong(), true);
    }


    public ValidateTokenResponseDto validateRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            return new ValidateTokenResponseDto(null, false);
        }

        DecodedJWT decodedRefreshToken = decodeJwt(refreshToken);

        if(isExpiredToken(decodedRefreshToken)) {
            return new ValidateTokenResponseDto(null, false);
        }

        Long userId = decodedRefreshToken.getClaim("userId").asLong();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!isVerifiedSignature(decodedRefreshToken, user)) {
            return new ValidateTokenResponseDto(null, false);
        }

        return new ValidateTokenResponseDto(userId, true);
    }

    private String getAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(JwtProperties.ACCESS_TOKEN_HEADER);
        return getAccessToken(authorizationHeader);
    }

    private String getAccessToken(StompHeaderAccessor headerAccessor) {
        String authorizationHeader = headerAccessor.getFirstNativeHeader(JwtProperties.ACCESS_TOKEN_HEADER);
        return getAccessToken(authorizationHeader);
    }

    private static String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(JwtProperties.ACCESS_TOKEN_PREFIX)) {
            return authorizationHeader.replace(JwtProperties.ACCESS_TOKEN_PREFIX, "");
        }
        return null;
    }

    public DecodedJWT decodeJwt(String token){
        try {
            return JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(token);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            // Token 이 있으나 복호화 실패
            throw new CustomException(ErrorCode.TOKEN_DECRYPTION_FAILURE);
        }
    }

    public boolean isExpiredToken(DecodedJWT decodedToken){
        Instant exp = decodedToken.getClaim("exp").asInstant();
        return exp.isBefore(Instant.now());
    }


    public boolean isVerifiedSignature(DecodedJWT decodedRefreshToken, User user) {
        if(user.getSignature() == null) return false;
        return user.getSignature().equals(decodedRefreshToken.getSignature());
    }

}