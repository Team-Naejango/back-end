package com.example.naejango.global.auth.filter;

import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.PrincipalDetails;
import com.example.naejango.global.auth.dto.TokenValidateResponse;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.auth.jwt.JwtValidator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private final JwtValidator jwtValidator;
    private final JwtGenerator jwtGenerator;
    private final UserRepository userRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtValidator jwtValidator, JwtGenerator jwtGenerator, UserRepository userRepository) {
        super(authenticationManager);
        this.jwtValidator = jwtValidator;
        this.jwtGenerator = jwtGenerator;
        this.userRepository = userRepository;
    }

    /**
     * JwtAuthorizationFilter
     * 인증이 필요한 api 접근시, jwt 검증 수행
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String accessToken = this.getAccessToken(request);

        // access token 의 소유 여부 확인
        if (accessToken != null) {
            // access token 이 있는 경우
            // access token 의 유효성을 검증
            TokenValidateResponse accessTokenValidateResponse = jwtValidator.validateAccessToken(accessToken);
            if (accessTokenValidateResponse.isValidToken()) {
                authenticate(accessTokenValidateResponse.getUserKey());
                chain.doFilter(request, response);
                return;
            }
        }
        // access token 이 없는 경우
        // refresh token 소유 여부를 확인
        String refreshToken = this.getRefreshToken(request);

        // refresh token 이 있는 경우
        if (refreshToken != null) {
            User user = getUser(refreshToken);
            TokenValidateResponse refreshTokenValidateResponse = jwtValidator.validateRefreshToken(refreshToken, user);
            // refresh token 의 유효성을 검증
            // refresh token이 유효한 경우
            if (refreshTokenValidateResponse.isValidToken()) {
                // access token 을 재발행하여 header 에 담아서 응답
                String reissuedAccessToken = jwtGenerator.generateAccessToken(user);
                response.setHeader(JwtProperties.ACCESS_TOKEN_HEADER, JwtProperties.ACCESS_TOKEN_PREFIX + reissuedAccessToken);
                authenticate(user.getUserKey());
                chain.doFilter(request, response);
                return;
            }
        }

        // refresh token 이 없는 경우
        chain.doFilter(request, response);
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

    public User getUser(String userKey) {
        return userRepository.findByUserKey(userKey).orElseThrow(()->{
            throw new IllegalArgumentException("회원을 찾지 못하였습니다.");
        });
    }

    public String getAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(JwtProperties.ACCESS_TOKEN_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(JwtProperties.ACCESS_TOKEN_PREFIX)) {
            return authorizationHeader.replace(JwtProperties.ACCESS_TOKEN_PREFIX, "");
        }
        return null;
    }

    public String getRefreshToken(HttpServletRequest request) {
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
