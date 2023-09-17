package com.example.naejango.domain.user.api;

import com.example.naejango.domain.account.application.AccountService;
import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.dto.CreateUserProfileCommandDto;
import com.example.naejango.domain.user.dto.ModifyUserProfileCommandDto;
import com.example.naejango.domain.user.dto.MyProfileDto;
import com.example.naejango.domain.user.dto.UserProfileDto;
import com.example.naejango.domain.user.dto.request.CreateUserProfileRequestDto;
import com.example.naejango.domain.user.dto.request.ModifyUserProfileRequestDto;
import com.example.naejango.domain.user.dto.response.UserProfileResponseDto;
import com.example.naejango.global.auth.jwt.JwtCookieHandler;
import com.example.naejango.global.auth.jwt.JwtValidator;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


/**
 * User 및 UserProfile Entity 의 api controller
 * Use
 */
@RequestMapping("/api/user")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final JwtValidator jwtValidator;
    private final JwtCookieHandler jwtCookieHandler;
    private final UserService userService;
    private final AccountService accountService;
    private final AuthenticationHandler authenticationHandler;

    /**
     * 회원 가입
     * 유저 프로필 및 계좌 등록
     * 유저 프로필 및 계좌를 등록하여 회원가입을 마무리 합니다.
     */
    @PostMapping("/profile")
    public ResponseEntity<CommonResponseDto<Void>> createUserProfile(@RequestBody @Valid CreateUserProfileRequestDto requestDto,
                                                  Authentication authentication) {
        // 요청 정보를 로드 합니다.
        Long userId = authenticationHandler.getUserId(authentication);
        var commandDto = new CreateUserProfileCommandDto(userId, requestDto);

        // 유저 프로필 생성
        userService.createUserProfile(commandDto);

        // 계좌 생성
        accountService.createAccount(userId);
        return ResponseEntity.ok().body(new CommonResponseDto<>("유저 프로필과 계좌가 생성되었습니다. 회원가입 처리가 되었습니다.", null));
    }

    /** 내 프로필 조회 */
    @GetMapping("/profile")
    public ResponseEntity<CommonResponseDto<MyProfileDto>> myProfile(Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        UserProfileDto userProfile = userService.findOtherUserProfile(userId);
        int balance = accountService.getAccount(userId);

        return ResponseEntity.ok().body(
                new CommonResponseDto<>("조회 성공", MyProfileDto.builder()
                .userId(userId)
                .nickname(userProfile.getNickname())
                .intro(userProfile.getIntro())
                .phoneNumber(userProfile.getPhoneNumber())
                .imgUrl(userProfile.getImgUrl())
                .birth(userProfile.getBirth())
                .gender(userProfile.getGender())
                .balance(balance).build()));
    }

    /** 다른 유저 프로필 조회 */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<CommonResponseDto<UserProfileResponseDto>> findUserProfile(@PathVariable Long userId) {
        UserProfileDto userProfile = userService.findOtherUserProfile(userId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("조회 완료",
                UserProfileResponseDto.builder()
                        .nickname(userProfile.getNickname())
                        .birth(userProfile.getBirth())
                        .gender(userProfile.getGender())
                        .imgUrl(userProfile.getImgUrl())
                        .intro(userProfile.getIntro())
                        .lastLogin(userProfile.getLastLogin()).build()
        ));
    }

    /** 프로필 수정 */
    @PatchMapping("/profile")
    public ResponseEntity<CommonResponseDto<Void>> modifyUserProfile(@RequestBody @Valid ModifyUserProfileRequestDto requestDto,
                                                                     Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        var commandDto = new ModifyUserProfileCommandDto(userId, requestDto);
        userService.modifyUserProfile(commandDto);

        return ResponseEntity.ok().body(new CommonResponseDto<>("수정 완료", null));
    }

    /** 유저 삭제 */
    @DeleteMapping("")
    public ResponseEntity<?> deleteUser(HttpServletRequest request, Authentication authentication) throws CustomException {
        Long userId = authenticationHandler.getUserId(authentication);

        jwtCookieHandler.getRefreshToken(request)
                .ifPresent(refreshToken -> jwtValidator.validateRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_DELETE_REQUEST)));

        userService.deleteUser(userId);
        return ResponseEntity.ok().body("구현 예정 입니다.");
    }

}
