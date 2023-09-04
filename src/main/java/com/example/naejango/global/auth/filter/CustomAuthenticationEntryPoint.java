package com.example.naejango.global.auth.filter;

import com.example.naejango.global.auth.jwt.AccessTokenReissuer;
import com.example.naejango.global.auth.jwt.JwtCookieHandler;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.TokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JwtCookieHandler jwtCookieHandler;
    private final AccessTokenReissuer accessTokenReissuer;

    /**
     * Authentication 에 실패한 경우 진입하는 EntryPoint 입니다.
     * 다른 Authentication 을 진행하지 않고 에러메세지를 반환하기 위해
     * Custom 한 Exception 을 throw 하였습니다.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null && jwtCookieHandler.hasRefreshTokenCookie(request)) {
            jwtCookieHandler.deleteAccessTokenCookie(request, response);
            throw new TokenException(ErrorCode.ACCESS_TOKEN_REISSUE, accessTokenReissuer.reissueAccessToken(request));
        }

        if(authentication == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }

    }
}
