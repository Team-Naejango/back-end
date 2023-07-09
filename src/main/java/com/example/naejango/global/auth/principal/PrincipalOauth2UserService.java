package com.example.naejango.global.auth.principal;

import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.oauth.Oauth2UserInfo;
import com.example.naejango.global.auth.oauth.kakao.KakaoUserInfo;
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

    /**
     * OAuth2.0 를 통해 로그인 된 요청을 처리하는 메서드
     * 프로필 서버에서 사용자 정보를 load 하여 OAuth2User 객체를 생성함
     * OAuth2User 객체의 사용자 정보가 DB에 있는 경우(회원가입이 된 경우)
     * DB 에서 사용자 정보를 조회하여 OAuth2User 에 담아서 반환하고
     * DB에 없는 경우(처음 로그인을 한 경우) DB에 User 객체를 저장하여 반환한다
     * @param userRequest : OAuth2UserRequest (accessToken, clientRegistration, additionalParameters)
     * @return OAuth2User : Attributes, Name, Collection<? extends GrantedAuthority>
     * @throws OAuth2AuthenticationException
     * =========================================================
     * OAuth2UserRequest 에는 로그인 된 사용자의 정보를 받아오기 위한
     * access token, api url 등 다양한 파라미터가 담겨있고
     * super.loadUser 메서드를 통해  OAuth2User 객체를 로드함
     * (이 과정에서 RestTemplate 이용한 통신 수행)
     * =========================================================
     * OAuth2User 는 사용자가 공개를 동의한 만큼의 사용자 정보가 담겨 있는 객체
     * 고유 id, 정보 제공 날짜 및 이메일, 닉네임 (동의 시) 등의 정보가 있으며
     * GrantedAuthority 객체는 ROLE, SCOPE(정보 열람 범위) 을 담고 있다.
     * =========================================================
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        Oauth2UserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        Optional<User> userOptional = userRepository.findByUserKey(kakaoUserInfo.getUserKey());

        User loginUser;
        if (userOptional.isEmpty()) {
            Long joinUserId = userService.join(kakaoUserInfo);
            loginUser = userService.findUser(joinUserId);
        } else {
            loginUser = userOptional.get();
        }

        return new PrincipalDetails(loginUser);
    }
}
