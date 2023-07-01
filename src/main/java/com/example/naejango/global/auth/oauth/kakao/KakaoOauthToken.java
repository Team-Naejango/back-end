package com.example.naejango.global.auth.oauth.kakao;

import lombok.Data;

@Data
public class KakaoOauthToken {
    private String token_type;
    private String access_token;
    private String refresh_token;
    private int expires_in;
    private int refresh_token_expires_in;
    private String scope;
}
