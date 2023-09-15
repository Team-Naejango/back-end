package com.example.naejango.domain.follow.api;

import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.domain.follow.application.FollowService;
import com.example.naejango.domain.follow.dto.response.FindFollowResponseDto;
import com.example.naejango.global.common.util.AuthenticationHandler;
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
    public ResponseEntity<CommonResponseDto<List<FindFollowResponseDto>>> findFollow(Authentication authentication){
        Long userId = authenticationHandler.getUserId(authentication);
        List<FindFollowResponseDto> findFollowResponseDtoList = followService.findFollow(userId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("조회 성공", findFollowResponseDtoList));
    }

    /** 창고 팔로우 등록 */
    @PostMapping("/{storageId}")
    public ResponseEntity<CommonResponseDto<Void>> addFollow(Authentication authentication, @PathVariable Long storageId){
        Long userId = authenticationHandler.getUserId(authentication);
        followService.addFollow(userId, storageId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("등록 성공", null));
    }

    /** 창고 팔로우 해제 */
    @DeleteMapping("/{storageId}")
    public ResponseEntity<CommonResponseDto<Void>> deleteFollow(Authentication authentication, @PathVariable Long storageId){
        Long userId = authenticationHandler.getUserId(authentication);
        followService.deleteFollow(userId, storageId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("해제 성공", null));
    }

}
