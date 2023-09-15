package com.example.naejango.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true); // cross origin 으로부터 인증을 위한 쿠키 정보를 받을지 여부
        config.addAllowedOrigin("https://localhost:3000");
        config.addAllowedOrigin("http://localhost:5500");
        config.addAllowedOrigin("https://naejango.site");
        config.addAllowedHeader("*"); // Access-Control-Request-Headers
        config.addAllowedMethod("*"); // Access-Control-Request-Method

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
