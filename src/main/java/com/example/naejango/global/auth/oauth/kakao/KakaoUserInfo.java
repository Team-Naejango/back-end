package com.example.naejango.global.auth.oauth.kakao;

import com.example.naejango.global.auth.oauth.Oauth2UserInfo;

import java.util.Map;

public class KakaoUserInfo implements Oauth2UserInfo {

    /**
     * kakao 로부터 가져온 회원정보를 담는 객체입니다.
     * 본 프로젝트에서는 kakao 회원정보 중 회원의 고유 id 만을 사용합니다.
     * 그외 이미지, 닉네임, 이메일 등의 회원 정보는 전혀 사용하지 않지만
     * 추후 변경의 여지가 있기 때문에 몇몇 메서드는 구현했습니다.
     * 해당 메서드는 카카오에서 회원정보를 응답하는 형식에 따라 작성되었으며
     * 응답 형식은 kakao developers 에서 확인할 수 있습니다.
     */

    private final Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes){
        this.attributes = attributes;
    }

    @Override
    public String getUserKey() {
        return "kakao_" + getId();
    }

    @Override
    public long getId() {
        return (long) attributes.get("id");
    }

    @Override
    public String getName() {
        return (String) kakaoProfile().get("nickname");
    }

    public Map<String, Object> kakaoAccount() {
        return (Map<String, Object>) attributes.get("kakao_account");
    }

    public Map<String, Object> kakaoProfile() {
        return (Map<String, Object>) kakaoAccount().get("profile");
    }

}
