package com.example.naejango.domain.chat.config;

import com.example.naejango.domain.chat.dto.WebSocketMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    /**
     * RedisMessageListenerContainer 를 구성합니다.
     * RedisMessageListenerContainer 는 Redis 의 Pub/Sub 을 관리하는 컨테이너로,
     * 구독 대상이 되는 채널 (ChannelTopic 클래스) 과 해당 채널에 메세지가 발행되었을 때
     * 이를 핸들링 하는 메서드(MessageListener) 를 등록해 줄 수 있습니다.
     * @param redisConnectionFactory Redis 서버와의 연결 정보
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                       MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(redisConnectionFactory);
        listenerContainer.addMessageListener(listenerAdapter, new PatternTopic("chat"));
        return listenerContainer;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(RedisMessageListener listener) {
        return new MessageListenerAdapter(listener);
    }

    /**
     * RedisTemplate 를 등록합니다.
     * Redis 는 모든 데이터를 byte 배열로 직렬화하여 저장합니다. 때문에 시리얼라이저의 설정이 중요합니다.
     * 기본적으로 JdkSerializationRedisSerializer 가 사용되지만 성능, 호환성 등의 문제로 권장되는 설정이 아니므로,
     * key 값은 문자열로 직-역직렬화 하기 위한 StringRedisSerializer 를 적용하였고
     * value 값은 Json 형태로 저장하기 위해 Jackson 시리얼라이저를 적용하였습니다.
     * @param redisConnectionFactory Redis 서버와의 연결 정보
     */
    @Bean
    public RedisTemplate<String, WebSocketMessageDto> MessageRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, WebSocketMessageDto> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 시리얼라이저 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(WebSocketMessageDto.class));
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Long> LongRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Long> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 시리얼라이저 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Long.class));
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Object> defaultRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 시리얼라이저 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<String, Boolean> booleanRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Boolean> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 시리얼라이저 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Boolean.class));
        return redisTemplate;
    }


}
