package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.domain.MessageType;
import com.example.naejango.domain.chat.dto.WebSocketMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "redis-config.websocket", havingValue = "false")
@RequiredArgsConstructor
public class BasicWebSocketService implements WebSocketService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final String CHANNEL_PREFIX = "/sub/channel/";

    @Override
    public void publishMessage(String channelId, Object message) {
        simpMessagingTemplate.convertAndSend(CHANNEL_PREFIX + channelId, message);
    }

    @Override
    public void subscribeChannel(String channelId, Long userId) {
        /* SimpleMessageBroker 를 사용하고 있으므로 이전의 로직에서 세션이 저장됩니다. */
        WebSocketMessageDto subscribingMessage = WebSocketMessageDto.builder()
                .messageType(MessageType.ENTER)
                .channelId(Long.valueOf(channelId))
                .userId(userId)
                .content("채널에 입장하였습니다.").build();
        simpMessagingTemplate.convertAndSend(CHANNEL_PREFIX + channelId, subscribingMessage);
    }
}
