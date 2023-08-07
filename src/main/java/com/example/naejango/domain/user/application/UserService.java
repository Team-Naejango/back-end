package com.example.naejango.domain.user.application;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.dto.request.ModifyUserProfileRequestDto;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.auth.jwt.JwtValidator;
import com.example.naejango.global.auth.oauth.OAuth2UserInfo;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtValidator jwtValidator;

    @Transactional
    public Long join(OAuth2UserInfo oauth2UserInfo){
        User newUser = User.builder()
                .userKey(oauth2UserInfo.getUserKey())
                .password("null")
                .role(Role.TEMPORAL)
                .build();
        userRepository.save(newUser);
        return newUser.getId();
    }

    @Transactional
    public void createUserProfile(UserProfile userProfile, Long userId) {
        userProfileRepository.save(userProfile);
        User persistenceUser = findUser(userId);
        persistenceUser.createUserProfile(userProfile);
    }

    @Transactional
    public void modifyUserProfile(ModifyUserProfileRequestDto requestDto, Long userId) {
        User persistenceUserWithProfile = findUserWithProfile(userId);
        UserProfile persistenceUserProfile = persistenceUserWithProfile.getUserProfile();
        persistenceUserProfile.modifyUserProfile(requestDto);
    }


    @Transactional
    public void refreshSignature(Long userId, String refreshToken){
        User persistenceUser = findUser(userId);
        persistenceUser.refreshSignature(JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(refreshToken).getSignature());
    }

    @Transactional
    public ResponseEntity<Void> deleteUser(HttpServletRequest request, Long userId) {
        String refreshToken = this.getRefreshToken(request);
        if (refreshToken == null || !jwtValidator.validateRefreshToken(refreshToken).isValidToken()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findUserWithProfileById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        userRepository.deleteUserById(userId);
        userProfileRepository.deleteById(user.getUserProfile().getId());
        return ResponseEntity.ok().build();
    }

    private String getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) return null;
        return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(JwtProperties.REFRESH_TOKEN_COOKIE_NAME))
                .map(Cookie::getValue)
                .findAny()
                .orElseGet(null);
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public User findUserWithProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

}
