package com.example.naejango.domain.item.api;

import com.example.naejango.domain.item.application.ItemService;
import com.example.naejango.domain.item.dto.request.RequestCreateItem;
import com.example.naejango.domain.item.dto.request.RequestModifyItem;
import com.example.naejango.domain.item.dto.response.ResponseCreateItem;
import com.example.naejango.domain.user.application.UserService;
import com.example.naejango.domain.user.domain.User;
import com.example.naejango.global.common.dto.BaseResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/item")
@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    private final UserService userService;

    /** 아이템 등록 */
    @PostMapping("")
    public ResponseEntity<BaseResponseDto> createItem(Authentication authentication, @RequestBody RequestCreateItem requestCreateItem) {
        User user = userService.getUser(authentication);
        ResponseCreateItem responseCreateItem = itemService.createItem(user, requestCreateItem);

        // Response dto를 반환 할 지 간단하게 상태코드와 메시지만 반환 할 지 결정해야함

        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponseDto(201, "success"));
    }

    /** 아이템 수정 */
    @PostMapping("/")
    public ResponseEntity<BaseResponseDto> modifyItem(Authentication authentication, @RequestBody RequestModifyItem requestModifyItem) {
        User user = userService.getUser(authentication);
        itemService.modifyItem(user, requestModifyItem);

        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponseDto(201, "success"));
    }

}
