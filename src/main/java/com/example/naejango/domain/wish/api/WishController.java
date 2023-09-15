package com.example.naejango.domain.wish.api;

import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.domain.wish.application.WishService;
import com.example.naejango.domain.wish.dto.response.FindWishResponseDto;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/wish")
@RestController
@RequiredArgsConstructor
public class WishController {
    private final WishService wishService;
    private final AuthenticationHandler authenticationHandler;

    /** 관심 목록 조회 */
    @GetMapping("")
    public ResponseEntity<CommonResponseDto<List<FindWishResponseDto>>> findWish(Authentication authentication){
        Long userId = authenticationHandler.getUserId(authentication);
        List<FindWishResponseDto> findWishResponseDtoList = wishService.findWish(userId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("조회 성공", findWishResponseDtoList));
    }

    /** 아이템 관심 등록 */
    @PostMapping("/{itemId}")
    public ResponseEntity<CommonResponseDto<Void>> addWish(Authentication authentication, @PathVariable Long itemId){
        Long userId = authenticationHandler.getUserId(authentication);
        wishService.addWish(userId, itemId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("등록 성공", null));
    }

    /** 아이템 관심 해제 */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<CommonResponseDto<Void>> deleteWish(Authentication authentication, @PathVariable Long itemId){
        Long userId = authenticationHandler.getUserId(authentication);
        wishService.deleteWish(userId, itemId);

        return ResponseEntity.ok().body(new CommonResponseDto<>("해제 성공", null));
    }

}
