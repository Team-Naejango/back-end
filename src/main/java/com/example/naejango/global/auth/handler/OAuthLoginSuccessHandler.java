package com.example.naejango.global.auth.handler;

import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.global.auth.jwt.JwtCookieHandler;
import com.example.naejango.global.auth.jwt.JwtIssuer;
import com.example.naejango.global.auth.jwt.JwtPayload;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthenticationHandler authenticationHandler;
    private final JwtIssuer jwtIssuer;
    private final JwtCookieHandler jwtCookieHandler;

    /**
     * OAuth 로그인 처리가 성공적으로 수행되어 Authentication 객체가 만들어 진 경우 실행되는 메서드.
     *  - 만약 RefreshToken 기존에 이미 있는 경우에는 해당 로그인 절차를 무시
     *  - RefreshToken 이 없는 경우에만 RefreshToken 을 발급
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        Optional<String> refreshToken = jwtCookieHandler.getRefreshToken(request);

        URL url = new URL(request.getHeader("Referer"));
        String protocol = request.isSecure()?"https":"http";
        String redirectUrl = protocol + "://" + url.getHost() + "/oauth/KakaoCallback";
        if (refreshToken.isPresent()){
            response.sendRedirect(redirectUrl + "?loginStatus=already_logged_in");
            return;
        }
        generateAndSetTokenCookies(authentication, response, redirectUrl);
    }

    private void generateAndSetTokenCookies(Authentication authentication, HttpServletResponse response, String redirectUrl) throws IOException {
        Long userId = authenticationHandler.getUserId(authentication);
        Role role = authenticationHandler.getRole(authentication);

        jwtIssuer.issueTokenCookie(new JwtPayload(userId, role), response);
        response.sendRedirect(redirectUrl + "?loginStatus=" + authenticationHandler.getRole(authentication).name());
    }
}