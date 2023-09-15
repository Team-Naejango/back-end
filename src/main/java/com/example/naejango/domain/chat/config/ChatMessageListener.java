package com.example.naejango.domain.chat.config;

import com.example.naejango.domain.chat.dto.request.WebSocketMessageReceiveDto;
import com.example.naejango.domain.chat.dto.WebSocketMessageSendDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


/**
 * 메세지를 수신하고 구독된 사용자들에게 메세지를 보내주는 메시지 리스너 입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageListener implements MessageListener {

    private final RedisTemplate<String, WebSocketMessageReceiveDto> redisTemplate;

    /*
     * 메세지를 실제로 발행해 주는 객체 입니다.
     * SimpMessageSendingOperations 는 레디스의 구독 정보와 상관 없이
     * 스프링 웹소켓의 심플 메세지 브로커에 따라서 메세지를 전송합니다.
     */
    private final SimpMessagingTemplate messageSender;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        WebSocketMessageReceiveDto receiveDto = (WebSocketMessageReceiveDto) redisTemplate.getValueSerializer().deserialize(message.getBody());
        if (receiveDto != null) {
            // 채팅을 실시간으로 읽고 있는 사람에게는 완전한 정보를 보내줍니다.
            messageSender.convertAndSend("/sub/channel/" + receiveDto.getChannelId(), new WebSocketMessageSendDto(receiveDto));

            // 채팅탭에 머물고 있는 사람은 보낸 사람의 정보를 주지 않습니다.
            receiveDto.setSenderId(null);
            messageSender.convertAndSend("/sub/lounge/" + receiveDto.getChannelId(), new WebSocketMessageSendDto(receiveDto));
        }
    }

    /**
     * RedisMessageListenerContainer 를 구성합니다.
     * RedisMessageListenerContainer 는 Redis 의 Pub/Sub 을 관리하는 컨테이너로,
     * 구독 대상이 되는 채널 (ChannelTopic 클래스) 과 해당 채널에 메세지가 발행되었을 때
     * 이를 핸들링 하는 메서드(MessageListener) 를 등록해 줄 수 있습니다.
     * @param redisConnectionFactory Redis 서버와의 연결 정보
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(redisConnectionFactory);
        listenerContainer.addMessageListener(new MessageListenerAdapter(this), new PatternTopic("chat"));
        return listenerContainer;
    }

}
