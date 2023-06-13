package com.example.naejango.domain.user.application;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.naejango.domain.user.dto.request.UserInfoModifyRequest;
import com.example.naejango.domain.user.entity.Role;
import com.example.naejango.domain.user.entity.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.PrincipalDetails;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.auth.jwt.JwtValidator;
import com.example.naejango.global.auth.kakao.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtValidator jwtValidator;

    @Transactional
    public void setSignature(User user, String refreshToken){
        User persistenceUser = findUser(user.getUserKey());
        persistenceUser.setSignature(JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(refreshToken).getSignature());
        userRepository.save(user);
    }

    public User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(()->{
            throw new IllegalArgumentException("회원을 찾을 수 없습니다. " + id);
        });
    }
    public User findUser(String userKey){
        return userRepository.findByUserKey(userKey).orElseThrow(()->{
            throw new IllegalArgumentException("회원을 찾을 수 없습니다. " + userKey);
        });
    }

    public User findUser(HttpServletRequest request) {
        String userKey = jwtValidator.getUserKey(request);
        if(userKey == null){
            return null;
        }
        return userRepository.findByUserKey(userKey).orElseThrow(()->{
            throw new IllegalArgumentException("회원을 찾지 못하였습니다.");
        });
    }

    @Transactional
    public User createUser(KakaoUserInfo kakaoUserInfo){
        User newUser = User.builder()
                .userKey(kakaoUserInfo.getUserkey())
                .role(Role.USER)
                .password(encoder.encode("null"))
                .build();
        userRepository.save(newUser);
        return newUser;
    }

    @Transactional
    public void modifyUserInfo(Authentication authentication, UserInfoModifyRequest userInfoModifyRequest) {
        User user = AuthenticationToUser(authentication);
        User persistenceUser = userRepository.findById(user.getId()).orElseThrow(() ->
        {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다. " + user.getId());
        });
        persistenceUser.setNickname(userInfoModifyRequest.getNickname());
        persistenceUser.setProfileImageUrl(userInfoModifyRequest.getProfileImageUrl());
        persistenceUser.setIntro(userInfoModifyRequest.getIntro());
    }


    @Transactional
    public ResponseEntity<Void> deleteUser(Authentication authentication, HttpServletRequest request) {
        User user = AuthenticationToUser(authentication);
        if(!jwtValidator.validateAccessToken(request).isValidToken() && !jwtValidator.validateRefreshToken(request, user).isValidToken()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User persistenceUser = findUser(user.getId());
        userRepository.deleteUserById(persistenceUser.getId());

        return ResponseEntity.ok().build();
    }
    public User AuthenticationToUser(Authentication authentication) {
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        return principal.getUser();
    }
}
