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


public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final JwtAuthenticator jwtAuthenticator;

    @Autowired
    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtAuthenticator jwtAuthenticator) {
        super(authenticationManager);
        this.jwtAuthenticator = jwtAuthenticator;
    }

    /**
     * JwtAuthorizationFilter
     * 인증이 필요한 api 접근시, jwt 검증 수행
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        jwtAuthenticator.jwtAuthenticate(request, response);
        chain.doFilter(request, response);
    }
}
