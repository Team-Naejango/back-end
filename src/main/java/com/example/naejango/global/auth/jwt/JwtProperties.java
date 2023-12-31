package com.example.naejango.global.auth.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class JwtProperties {
    @Value("${jwt.secretA}")
    private String secretA;
    @Value("${jwt.secretB}")
    private String secretB;
    @Value("${jwt.access-token.cookieName}")
    private String accessTokenCookieName;
    @Value("${jwt.access-token.header}")
    private String accessTokenHeader;
    @Value("${jwt.access-token.prefix}")
    private String accessTokenPrefix;
    @Value("${jwt.access-token.expiration-length}")
    private long accessTokenExpirationLength;
    @Value("${jwt.refresh-token.cookieName}")
    private String refreshTokenCookieName;
    @Value("${jwt.refresh-token.expiration-length}")
    private long refreshTokenExpirationLength;
    @Value("${jwt.iss}")
    private String iss;

    public static String SECRET_A;
    public static String SECRET_B;
    public static String ACCESS_TOKEN_COOKIE_NAME;
    public static String ACCESS_TOKEN_HEADER;
    public static String ACCESS_TOKEN_PREFIX;
    public static long ACCESS_TOKEN_EXPIRATION_TIME;
    public static String REFRESH_TOKEN_COOKIE_NAME;
    public static long REFRESH_TOKEN_EXPIRATION_TIME;
    public static String ISS;

    @PostConstruct
    public void init(){
        SECRET_A = secretA;
        SECRET_B = secretB;

        ACCESS_TOKEN_COOKIE_NAME = accessTokenCookieName;
        ACCESS_TOKEN_HEADER = accessTokenHeader;
        ACCESS_TOKEN_PREFIX = accessTokenPrefix;
        ACCESS_TOKEN_EXPIRATION_TIME = accessTokenExpirationLength;

        REFRESH_TOKEN_COOKIE_NAME = refreshTokenCookieName;
        REFRESH_TOKEN_EXPIRATION_TIME = refreshTokenExpirationLength;
        ISS = iss;
    }
}
