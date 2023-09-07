package com.example.naejango.domain.wish.api;

import com.example.naejango.domain.wish.application.WishService;
import com.example.naejango.domain.wish.dto.response.FindWishResponseDto;
import com.example.naejango.global.common.dto.BaseResponseDto;
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
    public ResponseEntity<List<FindWishResponseDto>> findWish(Authentication authentication){
        Long userId = authenticationHandler.getUserId(authentication);
        List<FindWishResponseDto> findWishResponseDtoList = wishService.findWish(userId);

        return ResponseEntity.ok().body(findWishResponseDtoList);
    }

    /** 아이템 관심 등록 */
    @PostMapping("/{itemId}")
    public ResponseEntity<BaseResponseDto> addWish(Authentication authentication, @PathVariable Long itemId){
        Long userId = authenticationHandler.getUserId(authentication);
        wishService.addWish(userId, itemId);

        return ResponseEntity.ok().body(new BaseResponseDto(200, "success"));
    }

    /** 아이템 관심 해제 */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<BaseResponseDto> deleteWish(Authentication authentication, @PathVariable Long itemId){
        Long userId = authenticationHandler.getUserId(authentication);
        wishService.deleteWish(userId, itemId);

        return ResponseEntity.ok().body(new BaseResponseDto(200, "success"));
    }

}
