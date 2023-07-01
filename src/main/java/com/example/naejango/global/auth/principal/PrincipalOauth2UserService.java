package com.example.naejango.global.auth.principal;

import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.oauth.kakao.KakaoUserInfo;
import com.example.naejango.global.auth.oauth.Oauth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserService userService;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("userRequest : {}", userRequest.getClientRegistration().getClientId());
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        if ( !userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
            throw new OAuth2AuthenticationException("유효하지 않은 인증입니다.");
        }

        Oauth2UserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());

        Optional<User> userOptional = userRepository.findByUserKey(kakaoUserInfo.getUserKey());

        User loginUser;
        if (userOptional.isEmpty()) {
            loginUser = userService.join(kakaoUserInfo);
        } else {
            loginUser = userOptional.get();
        }

        return new PrincipalDetails(loginUser, oAuth2User.getAttributes());
    }
}
