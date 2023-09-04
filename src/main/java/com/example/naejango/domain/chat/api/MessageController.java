package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.MessageService;
import com.example.naejango.domain.chat.domain.Message;
import com.example.naejango.domain.chat.dto.MessageDto;
import com.example.naejango.domain.chat.dto.response.RecentMessageResponseDto;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.util.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final AuthenticationHandler authenticationHandler;

    /**
     * 특정 채팅방의 최근 메세지를 불러 옵니다.
     * 해당 채팅의 모든 메세지는 읽음 처리 됩니다.
     * @param chatId 채팅방 id
     * @param page 조회할 페이지
     * @param size 조회할 결과물 수
     * @return 더 읽어올 메세지가 있는지(hasNext), 현재 조회한 페이지(page)
     *         메세지 정보 리스트 (List<MessageDto>):
     *         보낸사람 id(senderId), 내용(content), 보낸 시각(sentAt)
     */
    @GetMapping("/{chatId}/recent")
    public ResponseEntity<RecentMessageResponseDto> getRecentMessages(@PathVariable("chatId") Long chatId,
                                                                      @RequestParam(value = "page", defaultValue = "0") int page,
                                                                      @RequestParam(value = "size", defaultValue = "25") @Max(100) int size,
                                                                      Authentication authentication) {
        Long userId = authenticationHandler.getUserId(authentication);

        Page<Message> result = messageService.recentMessages(userId, chatId, page, size);
        if(result.isEmpty()) throw new CustomException(ErrorCode.MESSAGE_NOT_FOUND);
        RecentMessageResponseDto responseBody = new RecentMessageResponseDto(result.getNumber(), result.getSize(), result.hasNext(),
                result.get().map(MessageDto::new).collect(Collectors.toList()));
        messageService.readMessages(chatId);
        return ResponseEntity.ok().body(responseBody);
    }

}
