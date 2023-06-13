package com.example.naejango.global.auth.filter;

import com.example.naejango.domain.user.entity.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.PrincipalDetails;
import com.example.naejango.global.auth.dto.TokenValidateResponse;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.auth.jwt.JwtValidator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    private final UserRepository userRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("인증 또는 권한이 필요한 요청");
        TokenValidateResponse validateResponse = jwtValidator.validateToken(request);
        if(validateResponse.getUserKey()==null || (validateResponse.isValidAccessToken()&&validateResponse.isValidRefreshToken())){
            chain.doFilter(request, response);
            return;
        }
        if (validateResponse.isValidRefreshToken() && !validateResponse.isValidAccessToken()) {
            User user = userRepository.findByUserKey(validateResponse.getUserKey());
            String reIssuedAccessToken = jwtGenerator.generateAccessToken(user);
            response.setHeader(JwtProperties.ACCESS_TOKEN_HEADER, reIssuedAccessToken);
        }
        authenticate(validateResponse.getUserKey());
        chain.doFilter(request, response);
    }

    private void authenticate (String userKey){
        User user = userRepository.findByUserKey(userKey);
        PrincipalDetails principalDetails = new PrincipalDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principalDetails,
                null,
                principalDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            System.out.println("authority = " + authority.getAuthority());
        }
    }

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtGenerator jwtGenerator, JwtValidator jwtValidator, UserRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
        this.jwtGenerator = jwtGenerator;
        this.jwtValidator = jwtValidator;
    }


}
