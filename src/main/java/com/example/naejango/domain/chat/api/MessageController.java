package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.domain.Message;
import com.example.naejango.domain.chat.dto.MessageDto;
import com.example.naejango.domain.chat.dto.response.RecentMessageDto;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.chat.repository.MessageRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.handler.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final AuthenticationHandler authenticationHandler;

    @GetMapping("/{chatId}/recent")
    public ResponseEntity<RecentMessageDto> getRecentMessages(@PathVariable("chatId") Long chatId,
                                                              @RequestParam("page") int page,
                                                              @RequestParam("size") int size,
                                                              Authentication authentication) {
        Long userId = authenticationHandler.userIdFromAuthentication(authentication);
        if(chatRepository.countByIdAndOwnerId(chatId, userId) == 0) throw new CustomException(ErrorCode.UNAUTHORIZED);
        Page<Message> recentMessages = messageRepository.findRecentMessages(chatId, PageRequest.of(page, size));
        messageRepository.readMessageByChatId(chatId);
        if(recentMessages.isEmpty()) throw new CustomException(ErrorCode.MESSAGE_NOT_FOUND);
        RecentMessageDto result = new RecentMessageDto(recentMessages.getTotalElements(), recentMessages.stream().map(MessageDto::new).collect(Collectors.toList()));
        return ResponseEntity.ok().body(result);
    }

}
