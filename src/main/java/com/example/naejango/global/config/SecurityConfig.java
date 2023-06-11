package com.example.naejango.global.config;

import com.example.naejango.domain.user.entity.Role;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.PrincipalDetailsService;
import com.example.naejango.global.auth.PrincipalOauth2UserService;
import com.example.naejango.global.auth.filter.JwtAuthorizationFilter;
import com.example.naejango.global.auth.filter.LoginSuccessHandler;
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
    private final UserRepository userRepository;
    private final JwtValidator jwtValidator;
    private final PrincipalDetailsService principalDetailsService;
    private final PrincipalOauth2UserService principalOauth2UserService;
    private final LoginSuccessHandler loginSuccessHandler;
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(principalDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilter(corsConfig.corsFilter())
                .csrf().disable() // csrf 보안 필요하지 않으므로 disable 처리
                .formLogin().disable() // 기본 로그인 폼 사용안하므로 disable 처리
                .httpBasic().disable()// Http basic Auth 기반으로 로그인 인증창이 뜸 disable 시에 인증창 뜨지 않음
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용하지않고 Stateless 하게 만듬
                .and()
//                .addFilter(new JwtAuthenticationFilter(authenticationManager()))
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), jwtValidator, userRepository))
                .authorizeRequests()
                .antMatchers("/api/user/**")
                .hasAnyAuthority(Role.USER.toString(), Role.ADMIN.toString())
                .antMatchers("/api/admin/**")
                .hasAnyAuthority(Role.ADMIN.toString())
                .anyRequest().permitAll()
                .and()
                .oauth2Login()
                .loginPage("/loginPage")
                .userInfoEndpoint()
                .userService(principalOauth2UserService)
                .and()
                .successHandler(loginSuccessHandler);
        return http.build();
    }
}