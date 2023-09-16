package com.example.naejango.domain.chat.application.websocket;

import com.example.naejango.domain.chat.dto.WebSocketMessageCommandDto;
import com.example.naejango.domain.chat.dto.WebSocketMessageSendDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import static com.example.naejango.domain.chat.domain.MessageType.SUBSCRIBE_CHANNEL;
import static com.example.naejango.domain.chat.domain.MessageType.SUBSCRIBE_LOUNGE;

@Service
@ConditionalOnProperty(name = "redis-config.websocket", havingValue = "false")
@RequiredArgsConstructor
public class BasicWebSocketService implements WebSocketService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void publishMessage(WebSocketMessageCommandDto commandDto) {
        WebSocketMessageSendDto sendDto = commandDto.toSendDto();
        simpMessagingTemplate.convertAndSend(SUBSCRIBE_CHANNEL.getEndpointPrefix() + commandDto.getChannelId(), sendDto);
        simpMessagingTemplate.convertAndSend(SUBSCRIBE_LOUNGE.getEndpointPrefix() + commandDto.getChannelId(), sendDto.toLoungeMessage());
    }

}
