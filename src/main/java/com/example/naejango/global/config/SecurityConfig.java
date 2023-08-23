package com.example.naejango.global.config;

import com.example.naejango.global.auth.filter.CustomAuthenticationEntryPoint;
import com.example.naejango.global.auth.filter.ExceptionHandlingFilter;
import com.example.naejango.global.auth.filter.JwtAuthenticationFilter;
import com.example.naejango.global.auth.handler.AccessDeniedHandlerImpl;
import com.example.naejango.global.auth.handler.OAuthLoginFailureHandler;
import com.example.naejango.global.auth.handler.OAuthLoginSuccessHandler;
import com.example.naejango.global.auth.jwt.JwtAuthenticator;
import com.example.naejango.global.auth.principal.PrincipalOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.LinkedHashMap;

import static com.example.naejango.domain.user.domain.Role.*;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final PrincipalOAuth2UserService principalOauth2UserService;
    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
    private final OAuthLoginFailureHandler oAuthLoginFailureHandler;
    private final AccessDeniedHandlerImpl accessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final ExceptionHandlingFilter exceptionHandlingFilter;
    private final JwtAuthenticator jwtAuthenticator;
    private final CorsConfig corsConfig;

    @Bean
    public AuthenticationManager authenticationManager(){
        return new ProviderManager(new DaoAuthenticationProvider());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(corsConfig.corsFilter())
                .addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtAuthenticator))
                .addFilterAfter(exceptionHandlingFilter, JwtAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(delegatingAuthenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                .authorizeRequests()
                .antMatchers("/api/auth/**")
                .permitAll()
                .antMatchers(HttpMethod.POST, "/api/user/profile")
                .hasAnyRole(TEMPORAL.toString(), ADMIN.toString())
                .antMatchers("/api/user/**")
                .hasAnyRole(ADMIN.toString(), USER.toString(), GUEST.toString())
                .antMatchers("/ws/**").permitAll()
                .anyRequest().permitAll()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .userService(principalOauth2UserService)
                .and()
                .successHandler(oAuthLoginSuccessHandler)
                .failureHandler(oAuthLoginFailureHandler);
        return http.build();
    }

    /**
     * Authentication 이 실패하였을 때 다른 Custom EntryPoint 로 Delegating 해주는 Bean 입니다.
     * 모든 요청(/**) 에 대하여 적용하였습니다.
     */
    @Bean
    public DelegatingAuthenticationEntryPoint delegatingAuthenticationEntryPoint() {
        LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> entryPoints = new LinkedHashMap<>();
        entryPoints.put(new AntPathRequestMatcher("/**"), customAuthenticationEntryPoint);
        DelegatingAuthenticationEntryPoint delegatingEntryPoint = new DelegatingAuthenticationEntryPoint(entryPoints);
        delegatingEntryPoint.setDefaultEntryPoint(new BasicAuthenticationEntryPoint());
        return delegatingEntryPoint;
    }

}

