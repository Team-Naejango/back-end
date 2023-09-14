package com.example.naejango.domain.user.application;

import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.dto.CreateUserProfileCommandDto;
import com.example.naejango.domain.user.dto.ModifyUserProfileCommandDto;
import com.example.naejango.domain.user.dto.UserProfileDto;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.oauth.OAuth2UserInfo;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.WebSocketException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    /**
     * 유저 프로필을 생성
     */
    @Transactional
    public void createUserProfile(CreateUserProfileCommandDto commandDto) {
        // 유저를 로드합니다.
        User user = userRepository.findById(commandDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 유저 프로필을 생성합니다.
        UserProfile userProfile = commandDto.toEntity();
        userProfileRepository.save(userProfile);

        // 유저에 할당하고 Role 을 User 로 바꿉니다.
        user.setUserProfile(userProfile);
    }

    public UserProfileDto findOtherUserProfile(Long userId) {
        // 회원 로드
        User user = userRepository.findUserWithProfileById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 임시 회원의 경우 NOT_FOUND
        if(user.getRole().equals(Role.TEMPORAL)) throw new CustomException(ErrorCode.USER_NOT_FOUND);
        return new UserProfileDto(user.getUserProfile());
    }

    @Transactional
    public User join(OAuth2UserInfo oauth2UserInfo) {
        User newUser = User.builder()
                .userKey(oauth2UserInfo.getUserKey())
                .password("null")
                .role(Role.TEMPORAL)
                .build();
        return userRepository.save(newUser);
    }

    @Transactional
    public Long createGuest() {
        String uuid = UUID.randomUUID().toString();

        User guest = User.builder().userKey("Guest" + uuid)
                .role(Role.GUEST)
                .password("").build();

        userRepository.save(guest);

        UserProfile guestProfile = UserProfile.builder()
                .nickname("Guest")
                .phoneNumber("01012345678")
                .gender(Gender.MALE)
                .birth("20230701")
                .intro("서비스 둘러보기용 회원입니다.")
                .imgUrl("").build();

        userProfileRepository.save(guestProfile);
        guest.setUserProfile(guestProfile);

        return guest.getId();
    }

    @Transactional
    public void modifyUserProfile(ModifyUserProfileCommandDto commandDto) {
        UserProfile userProfile = userProfileRepository.findUserProfileByUserId(commandDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USERPROFILE_NOT_FOUND));

        userProfile.modifyUserProfile(commandDto.getNickname(), commandDto.getIntro(), commandDto.getImgUrl());
    }

    @Transactional
    public User login(Long userId) {
        User user = userRepository.findUserWithProfileById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!user.getRole().equals(Role.TEMPORAL)) {
            user.getUserProfile().setLastLogin();
        }
        return user;
    }

    @Transactional
    public User webSocketLogin(Long userId) {
        User user = userRepository.findUserWithProfileById(userId)
                .orElseThrow(() -> new WebSocketException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole().equals(Role.TEMPORAL)) {
            throw new WebSocketException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    @Transactional
    public void deleteUser(Long userId) throws CustomException {
        User user = userRepository.findUserWithProfileById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        /*
        삭제 로직 수정 필요
        User 와 연관된 객체들은 전부 삭제해야 함
        직접적으로 연관된 UserProfile, Account, Storage
        Storage 와 연관되어 있는 Item
        간접적으로 연관 되어있는 Chat
        Follow 등등....
         */
    }

}
