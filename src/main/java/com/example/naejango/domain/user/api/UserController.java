package com.example.naejango.domain.user.api;

import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.domain.user.domain.UserProfile;
import com.example.naejango.domain.user.dto.request.CreateUserProfileRequestDto;
import com.example.naejango.domain.user.dto.request.ModifyUserProfileRequestDto;
import com.example.naejango.domain.user.dto.response.UserProfileResponseDto;
import com.example.naejango.global.common.handler.CommonDtoHandler;
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
    private final UserService userService;
    private final CommonDtoHandler commonDtoHandler;

    @PostMapping("/profile")
    public ResponseEntity<Void> createUserProfile(@RequestBody @Valid CreateUserProfileRequestDto requestDto,
                                                  Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        UserProfile userProfile = new UserProfile(requestDto);
        userService.createUserProfile(userProfile, userId);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDto> userProfile(Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        User user = userService.findUser(userId);
        UserProfileResponseDto userProfileResponseDto = new UserProfileResponseDto(user.getUserProfile());
        return ResponseEntity.ok().body(userProfileResponseDto);
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserProfileResponseDto> modifyProfile(@RequestBody @Valid ModifyUserProfileRequestDto modifyUserProfileRequestDto, Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        userService.modifyUserProfile(modifyUserProfileRequestDto, userId);
        User user = userService.findUser(userId);
        return ResponseEntity.ok().body(new UserProfileResponseDto(user.getUserProfile()));
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteUser(HttpServletRequest request, Authentication authentication) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        return userService.deleteUser(request, userId);
    }

}
