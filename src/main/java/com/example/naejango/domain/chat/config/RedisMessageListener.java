package com.example.naejango.domain.chat.config;

import com.example.naejango.domain.chat.dto.WebSocketMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


/**
 * 메세지를 수신하고 구독된 사용자들에게 메세지를 보내주는 메시지 리스너 입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageListener implements MessageListener {

    private final RedisTemplate<String, WebSocketMessageDto> redisTemplate;

    /*
     * 메세지를 실제로 발행해 주는 객체 입니다.
     * SimpMessageSendingOperations 는 레디스의 구독 정보와 상관 없이
     * 스프링 웹소켓의 심플 메세지 브로커에 따라서 메세지를 전송합니다.
     */
    private final SimpMessagingTemplate messageSender;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        WebSocketMessageDto messageDto = (WebSocketMessageDto) redisTemplate.getValueSerializer().deserialize(message.getBody());
        messageSender.convertAndSend("/sub/channel/" + messageDto.getChannelId(), messageDto);
    }
}
