package com.example.naejango.domain.item.api;

import com.example.naejango.domain.item.application.ItemService;
import com.example.naejango.domain.item.dto.request.ConnectItemRequestDto;
import com.example.naejango.domain.item.dto.request.CreateItemRequestDto;
import com.example.naejango.domain.item.dto.request.ModifyItemRequestDto;
import com.example.naejango.domain.item.dto.response.CreateItemResponseDto;
import com.example.naejango.domain.item.dto.response.ModifyItemResponseDto;
import com.example.naejango.global.common.dto.BaseResponseDto;
import com.example.naejango.global.common.handler.CommonDtoHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    /** 아이템 정보 수정 */
    @PutMapping("/")
    public ResponseEntity<ModifyItemResponseDto> modifyItem(Authentication authentication, @RequestBody ModifyItemRequestDto modifyItemRequestDto) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        ModifyItemResponseDto modifyItemResponseDto = itemService.modifyItem(userId, modifyItemRequestDto);

        return ResponseEntity.ok().body(modifyItemResponseDto);
    }

    /** 아이템 창고 등록 수정 */
    @PutMapping("/connect")
    public ResponseEntity<BaseResponseDto> connectItem(Authentication authentication, @RequestBody ConnectItemRequestDto connectItemRequestDto) {
        Long userId = commonDtoHandler.userIdFromAuthentication(authentication);
        itemService.connectItem(userId, connectItemRequestDto);

        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponseDto(200, "success"));
    }

}
