package com.example.naejango.domain.chat.api;

import com.example.naejango.domain.chat.application.MessageService;
import com.example.naejango.domain.chat.domain.Message;
import com.example.naejango.domain.chat.dto.SendMessageRequestDto;
import com.example.naejango.domain.chat.dto.SendMessageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final MessageService messageService;

    @MessageMapping("/chat/{channelId}")
    @SendTo("/topic/chat/{channelId}")
    public SendMessageResponseDto sendMessage(@RequestBody SendMessageRequestDto requestDto, @DestinationVariable("channelId") Long channelId) {
        Message sentMessage = messageService.publishMessage(channelId, requestDto.getSenderId(), requestDto.getContent());
        return new SendMessageResponseDto(sentMessage);
    }

}