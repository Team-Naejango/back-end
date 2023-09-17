package com.example.naejango.domain.chat.application.websocket;

import com.example.naejango.domain.chat.domain.Channel;
import com.example.naejango.domain.chat.domain.MessageType;
import com.example.naejango.domain.chat.dto.WebSocketMessageCommandDto;
import com.example.naejango.domain.chat.repository.ChannelRepository;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "redis-config.websocket", havingValue = "true")
@RequiredArgsConstructor
public class RedisWebSocketService implements WebSocketService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelRepository channelRepository;

    public void publishMessage(WebSocketMessageCommandDto commandDto) {
        // 채널이 있는지 확인
        Channel channel = channelRepository.findById(commandDto.getChannelId())
                .orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));

        // 채널 종료 여부 확인, 퇴장 메세지는 발행합니다.
        if(channel.getIsClosed() && !commandDto.getMessageType().equals(MessageType.EXIT)) {
            throw new CustomException(ErrorCode.CHANNEL_IS_CLOSED);
        }

        redisTemplate.convertAndSend("chat", commandDto);
    }

}
