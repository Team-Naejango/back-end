package com.example.naejango.global.auth.jwt;

import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.dto.ValidateTokenResponseDto;
import com.example.naejango.global.auth.principal.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class JwtAuthenticator {
    private final JwtValidator jwtValidator;
    private final JwtGenerator jwtGenerator;
    private final UserRepository userRepository;

    public void jwtAuthenticate(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = this.getAccessToken(request);

        // access token 이 있는 경우
        if (accessToken != null) {
            // access token 유효성 검증 수행
            ValidateTokenResponseDto ValidateAccessTokenResponseDto = jwtValidator.validateAccessToken(accessToken);
            // access token 이 유효한 경우
            if (ValidateAccessTokenResponseDto.isValidToken()) {
                // Authenticate 진행
                authenticate(ValidateAccessTokenResponseDto.getUserKey());
                return;
            }
        }

        String refreshToken = this.getRefreshToken(request);

        // access token 은 없고 refresh token 이 있는 경우
        if (refreshToken != null) {
            User user = getUser(refreshToken);
            // refresh token 유효성 검증 수행
            ValidateTokenResponseDto refreshValidateTokenResponseDto = jwtValidator.validateRefreshToken(refreshToken, user);
            // refresh token 이 유효한 경우
            if (refreshValidateTokenResponseDto.isValidToken()) {
                // access token 을 재발행하여 cookie 로 응답
                String reissuedAccessToken = jwtGenerator.generateAccessToken(user);
                response.setHeader(JwtProperties.ACCESS_TOKEN_HEADER, JwtProperties.ACCESS_TOKEN_PREFIX + reissuedAccessToken); // 해결 할 것 (쿠키로 반환)
                // authenticate 진행
                authenticate(user.getUserKey());
            }
        }

    }

    /**
     * authenticate
     * Authentication 객체를 생성하여 SecurityContext 에 넣음
     * Authentication : UsernamePasswordAuthenticationToken
     * exception : jwtToken을 지니고 있는데 해당 회원이 없는 경우
     */
    private void authenticate (String userKey){
        User user = this.getUser(userKey);
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalDetails,
                null,
                principalDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private User getUser(String userKey) {
        return userRepository.findByUserKey(userKey).orElseThrow(()->{
            throw new IllegalArgumentException("회원을 찾지 못하였습니다.");
        });
    }

    private String getAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(JwtProperties.ACCESS_TOKEN_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(JwtProperties.ACCESS_TOKEN_PREFIX)) {
            return authorizationHeader.replace(JwtProperties.ACCESS_TOKEN_PREFIX, "");
        }
        return null;
    }

    private String getRefreshToken(HttpServletRequest request) {
        String refreshTokenCookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie != null && cookie.getName().equals(JwtProperties.REFRESH_TOKEN_HEADER)) {
                    refreshTokenCookie = cookie.getValue();
                }
            }
        }
        if (refreshTokenCookie != null && refreshTokenCookie.startsWith(JwtProperties.REFRESH_TOKEN_PREFIX)) {
            return refreshTokenCookie.replace(JwtProperties.REFRESH_TOKEN_PREFIX, "");
        }
        return null;
    }
}
