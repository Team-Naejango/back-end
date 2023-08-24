package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.domain.Message;
import com.example.naejango.domain.chat.dto.MessageDto;
import com.example.naejango.domain.chat.dto.response.RecentMessageDto;
import com.example.naejango.domain.chat.repository.MessageRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;

    @GetMapping("/{chatroomId}/recent")
    public ResponseEntity<RecentMessageDto> getRecentMessages(@PathVariable("chatroomId") Long chatroomId,
                                                              @RequestParam("page") int page,
                                                              @RequestParam("size") int size) {
        Page<Message> recentMessages = messageRepository.findRecentMessages(chatroomId, PageRequest.of(page, size));
        if(recentMessages.isEmpty()) throw new CustomException(ErrorCode.MESSAGE_NOT_FOUND);
        RecentMessageDto result = new RecentMessageDto(recentMessages.getTotalElements(), recentMessages.stream().map(MessageDto::new).collect(Collectors.toList()));
        return ResponseEntity.ok().body(result);
    }

}
