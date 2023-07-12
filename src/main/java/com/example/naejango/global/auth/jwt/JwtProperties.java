package com.example.naejango.global.auth.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
public class JwtProperties {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.access-token.header}")
    private String accessTokenHeader;
    @Value("${jwt.access-token.prefix}")
    private String accessTokenPrefix;
    @Value("${jwt.access-token.expiration-length}")
    private long accessTokenExpirationLength;
    @Value("${jwt.refresh-token.header}")
    private String refreshTokenCookie;
    @Value("${jwt.refresh-token.prefix}")
    private String refreshTokenPrefix;
    @Value("${jwt.refresh-token.expiration-length}")
    private long refreshTokenExpirationLength;
    @Value("${jwt.iss}")
    private String iss;

    public static String SECRET;
    public static String ACCESS_TOKEN_HEADER;
    public static String ACCESS_TOKEN_PREFIX;
    public static long ACCESS_TOKEN_EXPIRATION_TIME;
    public static String REFRESH_TOKEN_COOKIE;
    public static String REFRESH_TOKEN_PREFIX;
    public static long REFRESH_TOKEN_EXPIRATION_TIME;
    public static String ISS;

    @PostConstruct
    public void init(){
        SECRET = secret;

        ACCESS_TOKEN_HEADER = accessTokenHeader;
        ACCESS_TOKEN_PREFIX = accessTokenPrefix;
        ACCESS_TOKEN_EXPIRATION_TIME = accessTokenExpirationLength;

        REFRESH_TOKEN_COOKIE = refreshTokenCookie;
        REFRESH_TOKEN_PREFIX = refreshTokenPrefix;
        REFRESH_TOKEN_EXPIRATION_TIME = refreshTokenExpirationLength;
        ISS = iss;
    }
}
