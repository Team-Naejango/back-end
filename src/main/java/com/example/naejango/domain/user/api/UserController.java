package com.example.naejango.domain.user.api;

import com.example.naejango.domain.account.application.AccountService;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.dto.request.CreateUserProfileRequestDto;
import com.example.naejango.domain.user.dto.request.ModifyUserProfileRequestDto;
import com.example.naejango.domain.user.dto.response.ModifyUserProfileResponseDto;
import com.example.naejango.domain.user.dto.response.UserProfileResponseDto;
import com.example.naejango.domain.user.repository.UserProfileRepository;
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
    private final UserProfileRepository userProfileRepository;
    private final AccountService accountService;
    private final AuthenticationHandler authenticationHandler;

    @PostMapping("/profile")
    public ResponseEntity<Void> allocateUserProfile(@RequestBody @Valid CreateUserProfileRequestDto requestDto,
                                                    Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        UserProfile userProfile = new UserProfile(requestDto);
        userService.allocateUserProfile(userProfile, userId);
        accountService.createAccount(userId);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDto> findUserProfile(Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        UserProfile userProfile = userProfileRepository.findUserProfileByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USERPROFILE_NOT_FOUND));
        int balance = accountService.getAccount(userId);
        UserProfileResponseDto userProfileResponseDto = new UserProfileResponseDto(userProfile, balance);
        return ResponseEntity.ok().body(userProfileResponseDto);
    }

    @PatchMapping("/profile")
    public ResponseEntity<ModifyUserProfileResponseDto> modifyProfile(@RequestBody @Valid ModifyUserProfileRequestDto modifyUserProfileRequestDto, Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);
        userService.modifyUserProfile(modifyUserProfileRequestDto, userId);
        UserProfile userProfile = userProfileRepository.findUserProfileByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USERPROFILE_NOT_FOUND));
        return ResponseEntity.ok().body(new ModifyUserProfileResponseDto(userProfile));
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteUser(HttpServletRequest request, Authentication authentication) throws CustomException {
        Long userId = authenticationHandler.getUserId(authentication);
        String refreshToken = jwtCookieHandler.getRefreshToken(request);
        if (!jwtValidator.isValidRefreshToken(refreshToken).isValidToken()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_DELETE_REQUEST);
        }
        userService.deleteUser(userId);
        return ResponseEntity.ok().body("구현 예정 입니다.");
    }

}
