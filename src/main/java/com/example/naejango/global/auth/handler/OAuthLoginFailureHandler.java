package com.example.naejango.global.auth.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthLoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.error(exception.getMessage());
        exception.printStackTrace();
        String redirectUrl = getRedirectUrl(request);
        response.sendRedirect(redirectUrl + "?failure");
    }
    private String getRedirectUrl(HttpServletRequest request) throws MalformedURLException {
        URL url = new URL(request.getHeader("Referer"));
        String referrer = url.getHost();
        if(referrer.startsWith("dev")) referrer = "dev.naejango.site";
        else referrer = "naejango.site";
        return "https://" + referrer + "/oauth/KakaoCallback";
    }
}
