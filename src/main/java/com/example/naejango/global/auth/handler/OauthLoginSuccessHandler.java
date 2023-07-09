package com.example.naejango.global.auth.handler;

import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.global.auth.dto.LoginResponse;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.principal.PrincipalDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OauthLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserService userService;
    private final JwtGenerator jwtGenerator;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        PrincipalDetails userPrincipal = (PrincipalDetails) authentication.getPrincipal();
        response.getWriter().write(objectMapper.writeValueAsString(generateLoginResponse(userPrincipal, response)));
    }

    private LoginResponse generateLoginResponse(PrincipalDetails userPrincipal, HttpServletResponse response) throws IOException {
        Long userId = userPrincipal.getUser().getId();
        String accessToken = jwtGenerator.generateAccessToken(userId);
        String refreshToken = jwtGenerator.generateRefreshToken(userId);
        userService.refreshSignature(userId, refreshToken);
        // 프론트 local test 를 위한 redirect Url
        // cookie 로 전달할 예정
         String redirectUrl = "http://localhost:3000/oauth/kakaoCallback";
         response.sendRedirect(redirectUrl + "?accessToken=" + accessToken + "&refreshToken=" + refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                        .build();
    }
}
