package com.example.naejango.domain.user.application;

import com.example.naejango.domain.chat.application.http.ChatService;
import com.example.naejango.domain.follow.repository.FollowRepository;
import com.example.naejango.domain.storage.application.StorageService;
import com.example.naejango.domain.user.domain.Gender;
import com.example.naejango.domain.user.domain.Role;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.dto.CreateUserProfileCommandDto;
import com.example.naejango.domain.user.dto.ModifyUserProfileCommandDto;
import com.example.naejango.domain.user.dto.UserProfileDto;
import com.example.naejango.domain.user.repository.UserProfileRepository;
import com.example.naejango.domain.user.repository.UserRepository;
import com.example.naejango.domain.wish.repository.WishRepository;
import com.example.naejango.global.auth.jwt.JwtValidator;
import com.example.naejango.global.auth.oauth.OAuth2UserInfo;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.WebSocketException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FollowRepository followRepository;
    private final WishRepository wishRepository;
    private final JwtValidator jwtValidator;
    private final StorageService storageService;
    private final ChatService chatService;

    /** 유저 프로필 생성 (회원 가입) */
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

    /** 유저 생성 */
    @Transactional
    public User join(OAuth2UserInfo oauth2UserInfo) {
        User newUser = User.builder()
                .userKey(oauth2UserInfo.getUserKey())
                .password("null")
                .role(Role.TEMPORAL)
                .build();
        return userRepository.save(newUser);
    }

    /** 게스트 유저 생성 */
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

    /** 타 유저 프로필 조회 */
    public UserProfileDto findUserProfile(Long userId) {
        // 회원 로드
        User user = userRepository.findUserWithProfileById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 임시 회원의 경우 NOT_FOUND
        if(user.getRole().equals(Role.TEMPORAL)) throw new CustomException(ErrorCode.USER_NOT_FOUND);
        return new UserProfileDto(user.getUserProfile());
    }

    @Transactional
    public void modifyUserProfile(ModifyUserProfileCommandDto commandDto) {
        UserProfile userProfile = userProfileRepository.findUserProfileByUserId(commandDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USERPROFILE_NOT_FOUND));

        userProfile.modifyUserProfile(commandDto.getNickname(), commandDto.getIntro(), commandDto.getImgUrl());
    }

    /** 로그인 */
    @Transactional
    public User login(Long userId) {
        User user = userRepository.findUserWithProfileById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!user.getRole().equals(Role.TEMPORAL)) {
            user.getUserProfile().setLastLogin();
        }
        return user;
    }

    // Exception 을 현재 다 따로 만들었는데, 추상화에 대한 고려가 부족했던 것 같다. 리팩토링 필요...
    @Transactional
    public User webSocketLogin(Long userId) {
        User user = userRepository.findUserWithProfileById(userId)
                .orElseThrow(() -> new WebSocketException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole().equals(Role.TEMPORAL)) {
            throw new WebSocketException(ErrorCode.UNAUTHORIZED);
        }
        user.getUserProfile().setLastLogin();
        return user;
    }

    /** 회원 삭제
     * 연관되는 엔티티 처리
     * User: role 수정 (deleted) -> 재가입시 UserKey 를 변경
     * UserProfile: 삭제하지 않고 모두 삭제 회원용 프로퍼티로 수정
     * Storage: 삭제 (연관 Item, Transaction, GroupChannel 처리)
     * review: 그냥 두기
     * follow, wish: 모두 삭제 처리
     * chat: 모두 삭제 처리 (chatMessage 도 삭제됨)
     */
    @Transactional
    public void deleteUser(Long userId, HttpServletRequest request) throws CustomException {
        // 유저 로드
        User user = userRepository.findUserWithProfileById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 권한 확인 : RefreshToken 확인
        if (!jwtValidator.validateRefreshToken(request)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_DELETE_REQUEST))
                .equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_DELETE_REQUEST);
        }

        // 유저 권한 변경 및 유저 프로필 변경
        user.deleteUser();
        user.getUserProfile().deleteUserProfile();

        // Storage 삭제
        storageService.deleteStorageByUserId(userId);

        // Follow, Wish 삭제
        followRepository.deleteAllByUserId(userId);
        wishRepository.deleteAllByUserId(userId);

        // Chat 삭제 (채널 퇴장)
        chatService.deleteChatByUserId(userId);
    }

}
