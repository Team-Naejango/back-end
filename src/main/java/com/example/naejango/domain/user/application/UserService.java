package com.example.naejango.domain.user.application;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.naejango.domain.user.dto.request.UserInfoModifyRequest;
import com.example.naejango.domain.user.entity.User;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.PrincipalDetails;
import com.example.naejango.global.auth.dto.TokenValidateResponse;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.auth.jwt.JwtValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtValidator JwtValidator;

    @Transactional
    public void setSignature(User user, String refreshToken){
        User persistenceUser = userRepository.findById(user.getId()).orElseThrow(() ->
        {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다. " + user.getId());
        });
        persistenceUser.setSignature(JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(refreshToken).getSignature());
        userRepository.save(user);
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
    public ResponseEntity<?> deleteUser(Authentication authentication, HttpServletRequest request) {
        TokenValidateResponse tokenValidateResponse = JwtValidator.validateToken(request);
        if(!tokenValidateResponse.isValidAccessToken() || !tokenValidateResponse.isValidRefreshToken()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = AuthenticationToUser(authentication);
        User persistenceUser = userRepository.findById(user.getId()).orElseThrow(() ->
        {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다. " + user.getId());
        });
        userRepository.deleteUserById(persistenceUser.getId());

        return ResponseEntity.ok().build();
    }
    public User AuthenticationToUser(Authentication authentication) {
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        return principal.getUser();
    }
}
