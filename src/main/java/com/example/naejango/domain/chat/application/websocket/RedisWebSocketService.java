package com.example.naejango.domain.chat.application.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "redis-config.websocket", havingValue = "true")
@RequiredArgsConstructor
public class RedisWebSocketService implements WebSocketService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void publishMessage(String channelId, Object message) {
        redisTemplate.convertAndSend("chat", message);
    }

}
