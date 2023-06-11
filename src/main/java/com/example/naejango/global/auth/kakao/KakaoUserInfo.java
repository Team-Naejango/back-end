package com.example.naejango.global.auth.kakao;

import java.util.Map;

public class KakaoUserInfo implements Oauth2UserInfo{
    private Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes){
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public long getId() {
        return (long) attributes.get("id");
    }

    public String getUserkey() {
        return "kakao_"+getId();
    }

    @Override
    public String name() {
        return (String) kakaoProfile().get("nickname");
    }

    public Map<String, Object> kakaoAccount() {
        return (Map<String, Object>) attributes.get("kakao_account");
    }

    public Map<String, Object> kakaoProfile() {
        return (Map<String, Object>) kakaoAccount().get("profile");
    }






}
