package com.example.naejango.domain.follow.api;

import com.example.naejango.domain.follow.application.FollowService;
import com.example.naejango.domain.follow.dto.response.FindFollowResponseDto;
import com.example.naejango.global.common.dto.BaseResponseDto;
import com.example.naejango.global.common.handler.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/follow")
@RestController
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;
    private final AuthenticationHandler authenticationHandler;

    /** 팔로우 목록 조회 */
    @GetMapping("")
    public ResponseEntity<List<FindFollowResponseDto>> findFollow(Authentication authentication){
        Long userId = authenticationHandler.userIdFromAuthentication(authentication);
        List<FindFollowResponseDto> findFollowResponseDtoList = followService.findFollow(userId);

        return ResponseEntity.ok().body(findFollowResponseDtoList);
    }

    /** 창고 팔로우 등록 */
    @PostMapping("/{storageId}")
    public ResponseEntity<BaseResponseDto> addFollow(Authentication authentication, @PathVariable Long storageId){
        Long userId = authenticationHandler.userIdFromAuthentication(authentication);
        followService.addFollow(userId, storageId);

        return ResponseEntity.ok().body(new BaseResponseDto(200, "success"));
    }

    /** 창고 팔로우 해제 */
    @DeleteMapping("/{storageId}")
    public ResponseEntity<BaseResponseDto> deleteFollow(Authentication authentication, @PathVariable Long storageId){
        Long userId = authenticationHandler.userIdFromAuthentication(authentication);
        followService.deleteFollow(userId, storageId);

        return ResponseEntity.ok().body(new BaseResponseDto(200, "success"));
    }

}
