package com.example.naejango.domain.chat.application;

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

}
