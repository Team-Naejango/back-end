package com.example.naejango.global.config;

import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.filter.JwtAuthorizationFilter;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.jwt.JwtValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CorsConfig corsConfig;
    private final JwtValidator jwtValidator;
    private final JwtGenerator jwtGenerator;
    private final UserRepository userRepository;
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(){
        return new ProviderManager(new DaoAuthenticationProvider());
    }
    /**
     * Authentication 의 경우
     * 카카오 로그인 시 회원 키와 서명을 담은 jwt를 발급하고
     * Authorization 시 해당 jwt를 검증하여
     * 유효한 token일시 자체적인 로직으로 Authentication 을 주입하는 로직을 구현하고 있음
     * 때문에 별도의 Authentication 과정이 필요하지 않고
     * AuthenticationManger도 쓰이지 않음
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilter(corsConfig.corsFilter())
                .csrf().disable() // csrf 보안 필요하지 않으므로 disable 처리
                .formLogin().disable() // 기본 로그인 폼 사용안하므로 disable 처리
                .httpBasic().disable()// Http basic Auth 기반으로 로그인 인증창이 뜸 disable 시에 인증창 뜨지 않음
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용하지않고 Stateless 하게 만듬
                .and()
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), jwtValidator, jwtGenerator, userRepository))
                .authorizeRequests()
                .antMatchers("/api/user/**")
                .hasAnyRole(Role.USER.toString(), Role.ADMIN.toString())
                .antMatchers("/api/admin/**")
                .hasRole(Role.ADMIN.toString())
                .anyRequest().permitAll()
                .and()
                .oauth2Login()
                .loginPage("/loginPage");

        return http.build();
    }
}

