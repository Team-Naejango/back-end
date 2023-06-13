package com.example.naejango.global.auth.filter;

import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.entity.User;
import com.example.naejango.global.auth.PrincipalDetails;
import com.example.naejango.global.auth.dto.TokenValidateResponse;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.auth.jwt.JwtValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final JwtValidator jwtValidator;
    private final JwtGenerator jwtGenerator;
    @Autowired
    private UserService userService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("인증 또는 권한이 필요한 요청");

        // access token을 검증합니다.
        TokenValidateResponse accessTokenValidateResponse = jwtValidator.validateAccessToken(request);
        if(accessTokenValidateResponse.isValidToken()) {
            authenticate(accessTokenValidateResponse.getUserKey());
        }

        // refresh token을 검증합니다.
        String refreshToken = jwtValidator.getRefreshToken(request);
        if(refreshToken == null){
            chain.doFilter(request, response);
            return;
        }

        User user = userService.findUser(request);
        TokenValidateResponse refreshTokenValidateResponse = jwtValidator.validateRefreshToken(request, user);
        if (refreshTokenValidateResponse.isValidToken()) {
            String reissuedAccessToken = jwtGenerator.generateAccessToken(user);
            response.setHeader(JwtProperties.ACCESS_TOKEN_HEADER, reissuedAccessToken);
            authenticate(user.getUserKey());
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(request, response);
    }

    private void authenticate (String userKey){
        User user = userService.findUser(userKey);
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalDetails,
                null,
                principalDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtGenerator jwtGenerator, JwtValidator jwtValidator) {
        super(authenticationManager);
        this.jwtGenerator = jwtGenerator;
        this.jwtValidator = jwtValidator;
    }
}
