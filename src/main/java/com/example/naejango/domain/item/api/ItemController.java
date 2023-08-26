package com.example.naejango.domain.item.api;

import com.example.naejango.domain.item.application.ItemService;
import com.example.naejango.domain.item.dto.request.ConnectItemRequestDto;
import com.example.naejango.domain.item.dto.request.CreateItemRequestDto;
import com.example.naejango.domain.item.dto.request.ModifyItemRequestDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.dto.response.FindItemResponseDto;
import com.example.naejango.domain.item.dto.response.ModifyItemResponseDto;
import com.example.naejango.global.common.dto.BaseResponseDto;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequestMapping("/api/item")
@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final CommonDtoHandler commonDtoHandler;

    /** 아이템 생성 */
    @PostMapping("")
    public ResponseEntity<CreateItemResponseDto> createItem(Authentication authentication, @RequestBody CreateItemRequestDto createItemRequestDto) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        CreateItemResponseDto createItemResponseDto = itemService.createItem(userId, createItemRequestDto);
        return ResponseEntity.created(URI.create("/api/item/"+createItemResponseDto.getId())).body(createItemResponseDto);
    }

    /** 아이템 정보 조회 */
    @GetMapping("/{itemId}")
    public ResponseEntity<FindItemResponseDto> findItem(@PathVariable Long itemId) {
        FindItemResponseDto findItemResponseDto = itemService.findItem(itemId);

        return ResponseEntity.ok().body(findItemResponseDto);
    }

    /** 아이템 정보 수정 */
    @PatchMapping("/{itemId}")
    public ResponseEntity<ModifyItemResponseDto> modifyItem(Authentication authentication, @PathVariable Long itemId, @RequestBody ModifyItemRequestDto modifyItemRequestDto) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        ModifyItemResponseDto modifyItemResponseDto = itemService.modifyItem(userId, itemId, modifyItemRequestDto);

        return ResponseEntity.ok().body(modifyItemResponseDto);
    }

    /** 아이템 창고 등록 수정 */
    @PatchMapping("/connect/{itemId}")
    public ResponseEntity<BaseResponseDto> connectItem(Authentication authentication, @PathVariable Long itemId, @RequestBody ConnectItemRequestDto connectItemRequestDto) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        itemService.connectItem(userId, itemId,connectItemRequestDto);

        return ResponseEntity.ok().body(new BaseResponseDto(200, "success"));
    }

}
