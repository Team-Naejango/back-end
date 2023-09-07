package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.domain.MessageType;
import com.example.naejango.domain.chat.dto.WebSocketMessageDto;
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

    public void subscribeChannel(String channelId, Long userId) {
        WebSocketMessageDto messageDto = WebSocketMessageDto.builder()
                .messageType(MessageType.ENTER)
                .userId(userId)
                .channelId(Long.valueOf(channelId))
                .content("채널에 입장하였습니다.").build();
        System.out.println("RedisWebSocketService.subscribeChannel");
        redisTemplate.convertAndSend("chat", messageDto);
    }
}
