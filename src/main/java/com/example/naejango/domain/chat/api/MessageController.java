package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.http.MessageService;
import com.example.naejango.domain.chat.dto.MessageDto;
import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import java.util.List;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final AuthenticationHandler authenticationHandler;

    /**
     * 채팅 메세지 조회
     * 특정 채팅방의 최근 메세지를 불러 옵니다.
     * 해당 채팅의 모든 메세지는 읽음 처리 됩니다.
     */
    @GetMapping("/{chatId}")
    public ResponseEntity<CommonResponseDto<List<MessageDto>>> getRecentMessages(@PathVariable("chatId") Long chatId,
                                                                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                                                                 @RequestParam(value = "size", defaultValue = "25") @Max(300) int size,
                                                                                 Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        // 조회
        List<MessageDto> serviceDto = messageService.recentMessages(userId, chatId, page, size);

        // 반환
        return ResponseEntity.ok().body(new CommonResponseDto<>("조회 성공", serviceDto));
    }

}
