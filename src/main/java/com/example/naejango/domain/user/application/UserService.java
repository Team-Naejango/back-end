package com.example.naejango.domain.user.application;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.dto.request.UserInfoModifyRequest;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.dto.response.UserInfoResponse;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.oauth.Oauth2UserInfo;
import com.example.naejango.global.auth.principal.PrincipalDetails;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtValidator jwtValidator;

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(()->{
            throw new IllegalArgumentException("회원을 찾을 수 없습니다. " + id);
        });
    }
    public User getUser(String userKey){
        return userRepository.findByUserKey(userKey).orElseThrow(()->{
            throw new IllegalArgumentException("회원을 찾을 수 없습니다. " + userKey);
        });
    }

    public User getUser(Authentication authentication) {
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        return userRepository.findById(principal.getUser().getId()).orElseThrow(()->{
            throw new IllegalArgumentException("회원을 찾지 못하였습니다.");
        });
    }

    public UserInfoResponse getUserInfo(Authentication authentication){
        User user = getUser(authentication);
        return new UserInfoResponse(user.getUserProfile());
    }

    @Transactional
    public void setSignature(User user, String refreshToken){
        User persistenceUser = getUser(user.getUserKey());
        persistenceUser.setSignature(JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(refreshToken).getSignature());
    }

    @Transactional
    public User join(Oauth2UserInfo oauth2UserInfo){
        User newUser = User.builder()
                .userKey(oauth2UserInfo.getUserKey())
                .password("null")
                .role(Role.USER)
                .build();
        userRepository.save(newUser);
        return newUser;
    }

    @Transactional
    public void modifyUserInfo(Authentication authentication, UserInfoModifyRequest userInfoModifyRequest) {
        User user = getUser(authentication);
        User persistenceUser = userRepository.findById(user.getId()).orElseThrow(() ->
        {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다. " + user.getId());
        });
        persistenceUser.getUserProfile().modifyUserProfile(
                userInfoModifyRequest.getNickname(),
                userInfoModifyRequest.getIntro(),
                userInfoModifyRequest.getImgUrl()
        );
    }

    @Transactional
    public ResponseEntity<Void> deleteUser(Authentication authentication, HttpServletRequest request) {
        User user = getUser(authentication);
        String refreshTokenHeader = request.getHeader(JwtProperties.REFRESH_TOKEN_HEADER);
        if (refreshTokenHeader == null || !jwtValidator.validateRefreshToken(refreshTokenHeader.replace(JwtProperties.REFRESH_TOKEN_PREFIX, ""), user).isValidToken()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User persistenceUser = getUser(user.getId());
        userRepository.deleteUserById(persistenceUser.getId());
        return ResponseEntity.ok().build();
    }
}
