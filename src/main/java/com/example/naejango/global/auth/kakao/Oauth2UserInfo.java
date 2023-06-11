package com.example.naejango.global.auth.kakao;

public interface Oauth2UserInfo {

    String getProvider();
    long getId();
    String name();
}
