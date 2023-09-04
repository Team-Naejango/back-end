package com.example.naejango.domain.user.application;

import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.dto.request.ModifyUserProfileRequestDto;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.global.auth.oauth.OAuth2UserInfo;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
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

    @Transactional
    public User join(OAuth2UserInfo oauth2UserInfo){
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

        User guest = User.builder().userKey("Guest"+uuid)
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
    public void allocateUserProfile(UserProfile userProfile, Long userId) {
        userProfileRepository.save(userProfile);
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.setUserProfile(userProfile);
    }

    @Transactional
    public void modifyUserProfile(ModifyUserProfileRequestDto requestDto, Long userId) {
        UserProfile userProfile = userProfileRepository.findUserProfileByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USERPROFILE_NOT_FOUND));
        userProfile.modifyUserProfile(requestDto);
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
