package com.example.naejango.domain.user.application;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.dto.request.CreateUserProfileRequestDto;
import com.example.naejango.domain.user.dto.request.ModifyUserProfileRequestDto;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.oauth.Oauth2UserInfo;
import com.example.naejango.global.auth.jwt.JwtProperties;
import com.example.naejango.global.auth.jwt.JwtValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtValidator jwtValidator;

    @Transactional
    public Long join(Oauth2UserInfo oauth2UserInfo){
        User newUser = User.builder()
                .userKey(oauth2UserInfo.getUserKey())
                .password("null")
                .role(Role.TEMPORAL)
                .build();
        userRepository.save(newUser);
        return newUser.getId();
    }

    @Transactional
    public void createUserProfile(CreateUserProfileRequestDto requestDto, Long userId) {
        UserProfile userProfile = new UserProfile(requestDto);
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
        String refreshTokenHeader = request.getHeader(JwtProperties.REFRESH_TOKEN_HEADER);
        if (refreshTokenHeader == null || !jwtValidator.validateRefreshToken(refreshTokenHeader.replace(JwtProperties.REFRESH_TOKEN_PREFIX, "")).isValidToken()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        userRepository.deleteUserById(userId);
        return ResponseEntity.ok().build();
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(()->{
            throw new IllegalArgumentException("회원을 찾을 수 없습니다. userId: " + userId);
        });
    }

    public User findUserWithProfile(Long userId) {
        return userRepository.findById(userId).orElseThrow(()->{
            throw new IllegalArgumentException("회원을 찾을 수 없습니다. userId: " + userId);
        });
    }

}
