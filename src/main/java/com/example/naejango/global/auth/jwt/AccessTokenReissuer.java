package com.example.naejango.global.auth.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@RequiredArgsConstructor
public class AccessTokenReissuer {

    private final JwtValidator jwtValidator;
    private final JwtGenerator jwtGenerator;
    private final JwtCookieHandler jwtCookieHandler;

    public String reissueAccessToken(HttpServletRequest request) {
        String refreshToken = jwtCookieHandler.getRefreshToken(request);
        var validateResult = jwtValidator.isValidRefreshToken(refreshToken);
        if (!validateResult.isValidToken()) return null;
        return jwtGenerator.generateAccessToken(validateResult.getUserId());
    }

}
