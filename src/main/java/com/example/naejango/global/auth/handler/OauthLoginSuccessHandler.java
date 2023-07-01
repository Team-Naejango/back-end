package com.example.naejango.global.auth.handler;

import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.dto.LoginResponse;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.principal.PrincipalDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OauthLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtGenerator jwtGenerator;

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("login processing : {}", authentication.getName());
        log.info("login request Url: {}", request.getRequestURL());
        PrincipalDetails userPrincipal = (PrincipalDetails) authentication.getPrincipal();
        response.getWriter().write(objectMapper.writeValueAsString(generateLoginResponse(userPrincipal, response)));
    }

    private LoginResponse generateLoginResponse(PrincipalDetails userPrincipal, HttpServletResponse response) throws IOException {
        User user = userRepository.findByUserKey(userPrincipal.getUser().getUserKey()).orElseThrow(()->{
            throw new OAuth2AuthenticationException("회원을 찾을 수 없습니다.");
        });

        String accessToken = jwtGenerator.generateAccessToken(user);
        String refreshToken = jwtGenerator.generateRefreshToken(user);
        userService.setSignature(user, refreshToken);
        String redirectUrl = "/";
        response.sendRedirect(redirectUrl + "?accessToken=" + accessToken + "?refreshToken=" + refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                        .build();
    }
}
