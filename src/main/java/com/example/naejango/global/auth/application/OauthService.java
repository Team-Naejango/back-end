package com.example.naejango.global.auth.application;

import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.entity.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.dto.LoginResponse;
import com.example.naejango.global.auth.jwt.JwtGenerator;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.auth.kakao.KakaoOauthToken;
import com.example.naejango.global.auth.kakao.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OauthService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtGenerator jwtGenerator;
    private final InMemoryClientRegistrationRepository inMemoryClientRegistrationRepository;

    /**
     * 주석 추가 예정
     */
    public LoginResponse login(String providerName, String code){
        ClientRegistration provider = inMemoryClientRegistrationRepository.findByRegistrationId(providerName);

        KakaoOauthToken kakaoOauthToken = getToken(code, provider);
        Map<String, Object> kakaoProfileAttributes = getKakaoProfileAttributes(kakaoOauthToken, provider);
        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(kakaoProfileAttributes);

        User user = userRepository.findByUserKey(kakaoUserInfo.getUserkey()).orElse(null);
        if(user==null) {
            user = userService.createUser(kakaoUserInfo);
        }
        String accessToken = jwtGenerator.generateAccessToken(user);
        String refreshToken = jwtGenerator.generateRefreshToken(user);
        userService.setSignature(user, refreshToken.replace(JwtProperties.REFRESH_TOKEN_PREFIX, ""));
        return LoginResponse.builder()
                .id(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNew(!user.isCompleteProfile())
                .build();
    }

    /**
     * 카카오 서버로 부터 받은 code을 다시 카카오 서버로 전달하여
     * 사용자의 카카오 프로필에 접근할 수 있는 token 획득
     * RestTemplete -> Webclient 변경
     * @param code : 인가 코드
     * @param provider : Oauth provider
     * @return token : kakao 프로필 정보를 얻기 위해 사용하는 토큰
     */

    public KakaoOauthToken getToken(String code, ClientRegistration provider) {
        return WebClient.create()
                .post()
                .uri(provider.getProviderDetails().getTokenUri())
                .headers(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    httpHeaders.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                })
                .bodyValue(tokenRequest(code, provider))
                .retrieve()
                .bodyToMono(KakaoOauthToken.class) // response body 를 괄호안의 클래스에 자동으로 맵핑
                .block();
    }

    private MultiValueMap<String, String> tokenRequest(String code, ClientRegistration provider){
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", provider.getAuthorizationGrantType().getValue());
        params.add("client_id", provider.getClientId());
        params.add("redirect_uri", provider.getRedirectUri());
        params.add("code", code);
        return params;
    }
    /**
     * 카카오 서버로부터 받아온 Token으로 카카오 프로필을 요청하고
     * 응답을 다시 KakaoProfile 객체에 맵핑하여 반환*
     * RestTemplete -> Webclient 변경
     */

    /*
     * 로그인 처리시 프론트에 최종적으로 User를 넘겨줘야 하는데
     * 카카오 프로필을 얼마나 요청 할 것인지,
     * 어떤 식으로 맵핑할 것인지를 정해야 함
     *
     * 단순히 카카오 id만 받아와서
     * User 클래스와는 별도로 UserProfile 클래스를 만들어서
     * 관리하는 경우도 있음
     */
    public Map<String, Object> getKakaoProfileAttributes(KakaoOauthToken kakaoOauthToken, ClientRegistration provider) {
        return WebClient.create()
                .post()
                .uri(provider.getProviderDetails().getUserInfoEndpoint().getUri())
                .headers(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    httpHeaders.setBearerAuth(kakaoOauthToken.getAccess_token());
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }


}
