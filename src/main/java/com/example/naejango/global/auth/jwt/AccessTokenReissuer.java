package com.example.naejango.global.auth.jwt;

import com.example.naejango.global.auth.dto.ValidateTokenResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class AccessTokenReissuer {

    private final JwtValidator jwtValidator;
    private final JwtGenerator jwtGenerator;

    public String reissueAccessToken(HttpServletRequest request) {
        String refreshToken = getRefreshToken(request);
        if(refreshToken == null) return null;

        ValidateTokenResponseDto validateResult = jwtValidator.validateRefreshToken(refreshToken);
        if (!validateResult.isValidToken()) return null;

        return jwtGenerator.generateAccessToken(validateResult.getUserId());
    }

    /**
     * HttpServletRequest 에서 refresh token 을 가져오는 메서드
     * refresh cookie 가 없는 경우 null 을 반환
     */
    private String getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(JwtProperties.REFRESH_TOKEN_COOKIE_NAME))
                .map(Cookie::getValue)
                .findAny()
                .orElse(null);
    }

}
