package com.example.naejango.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    /**
     * RedisTemplate 를 등록합니다.
     * Redis 는 모든 데이터를 byte 배열로 직렬화하여 저장합니다. 때문에 시리얼라이저의 설정이 중요합니다.
     * 기본적으로 JdkSerializationRedisSerializer 가 사용되지만 성능, 호환성 등의 문제로 권장되는 설정이 아니므로,
     * key 값은 문자열로 직-역직렬화 하기 위한 StringRedisSerializer 를 적용하였고
     * value 값은 Json 형태로 저장하기 위해 Jackson 시리얼라이저를 적용하였습니다.
     * @param redisConnectionFactory Redis 서버와의 연결 정보
     */
    @Bean
    public RedisTemplate<String, Long> LongRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Long> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 시리얼라이저 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Long.class));
        return redisTemplate;
    }

    /** 이 템플릿에 한해서 Transactional 에 참여합니다. */
    @Bean
    public RedisTemplate<String, Object> defaultRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setEnableTransactionSupport(true);

        // 시리얼라이저 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return redisTemplate;
    }

    /*
      * OAuth Authorization 요청 시 요청 정보를 세션이 아닌 Redis 에 저장하기 위해
      * 직렬화 도구로 JdkSerializationRedisSerializer 를 적용하였습니다.
     */
    @Bean
    public RedisTemplate<String, OAuth2AuthorizationRequest> oAuthRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, OAuth2AuthorizationRequest> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

}