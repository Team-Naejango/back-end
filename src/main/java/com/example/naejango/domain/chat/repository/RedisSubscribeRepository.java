package com.example.naejango.domain.chat.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "redis-config.websocket", havingValue = "true")
@RequiredArgsConstructor
public class RedisSubscribeRepository implements SubscribeRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final String SESSION_USER = "Session_User:";
    private final String SESSION_SUBSCRIPTION = "Session_Subscription:";
    private final String SUBSCRIPTION_CHANNEL = "Subscription_Channel:";
    private final String CHANNEL_USER = "Channel_User:";

    @Override
    public void saveUserIdBySessionId(Long userId, String sessionId) {
        redisTemplate.opsForHash().putIfAbsent(SESSION_USER, sessionId, userId);
    }

    @Override
    public void saveSubscriptionIdBySessionId(String subscriptionId, String sessionId) {
        String key = SESSION_SUBSCRIPTION + sessionId ;
        redisTemplate.opsForSet().add(key, subscriptionId);
    }

    @Override
    public void setSubscriberToChannel(Long userId, Long channelId) {
        String key = CHANNEL_USER + channelId;
        redisTemplate.opsForSet().add(key, userId);
    }

    @Override
    public void setSubscriptionIdToChannel(String subscriptionId, Long channelId) {
        redisTemplate.opsForHash().put(SUBSCRIPTION_CHANNEL, subscriptionId, channelId);
    }

    @Override
    public Set<Long> findSubscribeChannelIdBySessionId(String sessionId) {
        String key = SESSION_SUBSCRIPTION + sessionId;
        Set<Object> subscriptions = redisTemplate.opsForSet().members(key);
        if(subscriptions == null) return new HashSet<>();
        return subscriptions.stream().map(subscription
                        -> (Long) redisTemplate.opsForHash().get(SUBSCRIPTION_CHANNEL, subscription))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> findSubscriptionIdBySessionId(String sessionId) {
        String key = SESSION_SUBSCRIPTION + sessionId;
        return redisTemplate.opsForSet().members(key)
                .stream().map(obj -> (String)obj).collect(Collectors.toSet());
    }
    @Override
    public Optional<Long> findUserIdBySessionId(String sessionId) {
        return Optional.ofNullable((Long) redisTemplate.opsForHash().get(SESSION_USER, sessionId));
    }

    @Override
    public Optional<Long> findChannelIdBySubscriptionId(String subscriptionId) {
        return Optional.ofNullable((Long) redisTemplate.opsForHash().get(SUBSCRIPTION_CHANNEL, subscriptionId));
    }

    @Override
    public Set<Long> findSubscribersByChannelId(Long channelId) {
        String key = CHANNEL_USER + channelId;
        return redisTemplate.opsForSet().members(key)
                .stream().map(obj -> Long.valueOf((Integer)obj)).collect(Collectors.toSet());
    }

    @Override
    public void deleteSessionId(String sessionId) {
        redisTemplate.opsForHash().delete(SESSION_USER, sessionId);
    }

    @Override
    public void deleteSubscriberFromChannel(Long userId, Long channelId) {
        String key = CHANNEL_USER + channelId;
        redisTemplate.opsForSet().remove(key, userId);
    }

    @Override
    public void deleteAllSubscriptionsBySessionId(String sessionId) {
        String key = SESSION_SUBSCRIPTION + sessionId;
        redisTemplate.delete(key);
    }

    @Override
    public void deleteSubscriptionId(String subscriptionId) {
        redisTemplate.opsForHash().delete(SUBSCRIPTION_CHANNEL, subscriptionId);
    }

    @Override
    public void deleteSubscriptionIdBySessionId(String subscriptionId, String sessionId) {
        String key = SESSION_SUBSCRIPTION + sessionId;
        redisTemplate.opsForSet().remove(key, subscriptionId);
    }
}
