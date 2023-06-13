package com.example.naejango.domain.user.api;

import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.dto.request.UserInfoModifyRequest;
import com.example.naejango.domain.user.dto.response.UserInfoResponse;
import com.example.naejango.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RequestMapping("/api/user")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<UserInfoResponse> userInfo(Authentication authentication) {
        User user = userService.AuthenticationToUser(authentication);
        UserInfoResponse userInfoResponse = new UserInfoResponse(user);
        return ResponseEntity.ok().body(userInfoResponse);
    }

    @PostMapping("/info")
    public ResponseEntity<UserInfoResponse> modifyInfo(Authentication authentication, @RequestBody UserInfoModifyRequest userInfoModifyRequest) {
        userService.modifyUserInfo(authentication, userInfoModifyRequest);
        User user = userService.AuthenticationToUser(authentication);
        UserInfoResponse userInfoResponse = new UserInfoResponse(user);
        return ResponseEntity.ok().body(userInfoResponse);
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteUser(Authentication authentication, HttpServletRequest request){
        return userService.deleteUser(authentication, request);
    }
}
