package com.example.naejango.global.auth.filter;

import com.example.naejango.global.auth.jwt.JwtAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    private final JwtAuthenticator jwtAuthenticator;

    @Autowired
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtAuthenticator jwtAuthenticator) {
        super(authenticationManager);
        this.jwtAuthenticator = jwtAuthenticator;
    }

    /**
     * JwtAuthorizationFilter
     * jwt를 검증하고 authenticate 해주는 필터
     * jwtAuthenticator 에서 jwt 가 유효함이 검증되면 authentication 을 생성해주며
     * jwt 가 없거나 유효하지 않으면 아무 작업을 수행하지 않고 그냥 반환
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        jwtAuthenticator.jwtAuthenticate(request, response);
        chain.doFilter(request, response);
    }
}
