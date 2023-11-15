package com.example.naejango.global.auth.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthLoginFailureHandler implements AuthenticationFailureHandler {
    private final String redirectUrl = "https://naejango.site/oauth/KakaoCallback";
    private final String localRedirectUrl = "http://localhost:3000/oauth/kakaoCallback";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.error(exception.getMessage());
        response.sendRedirect(redirectUrl + "?failure");
    }
}
