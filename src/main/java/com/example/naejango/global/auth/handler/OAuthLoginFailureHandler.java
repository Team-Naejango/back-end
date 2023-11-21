package com.example.naejango.global.auth.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthLoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.error(exception.getMessage());
        exception.printStackTrace();
        Iterator<String> iterator = request.getHeaderNames().asIterator();
        while(iterator.hasNext()){
            String next = iterator.next();
            System.out.println(next + " : " + request.getHeader(next));
        }
        URL url = new URL(request.getHeader("Referer"));
        String protocol = request.isSecure()?"https":"http";
        String redirectUrl = protocol + "://" + url.getHost() + "/oauth/KakaoCallback";
        response.sendRedirect(redirectUrl + "?failure");
    }
}
