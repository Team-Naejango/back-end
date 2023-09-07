package com.example.naejango.domain.chat.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
@ConditionalOnProperty(name = "redis-config.websocket", havingValue = "true")
@RequiredArgsConstructor
public class RedisSubscribeRepository implements SubscribeRepository {
    private final RedisTemplate<String, Long> longTemplate;
    private final RedisTemplate<String, Object> basicTemplate;
    private final StringRedisTemplate stringTemplate;

    @Override
    public void saveUserIdBySessionId(Long userId, String sessionId) {
        String key = "Session:" + sessionId;
        longTemplate.opsForValue().set(key, userId);
    }

    @Override
    public Optional<Long> findUserIdBySessionId(String sessionId) {
        String key = "Session:" + sessionId;
        return Optional.ofNullable(longTemplate.opsForValue().get(key));
    }

    @Override
    public void deleteSessionId(String sessionId) {
        String key = "Session:" + sessionId;
        longTemplate.opsForValue().getOperations().delete(key);
    }

    @Override
    public void setSubscriberToChannel(Long userId, Long channelId) {
        String key = "Channel:" + channelId;
        basicTemplate.opsForSet().add(key, userId);
    }
    
    @Override
    public Set<Long> findSubscribersByChannelId(Long channelId) {
        String key = "Channel:" + channelId;
        return longTemplate.opsForSet().members(key);
    }

    @Override
    public void deleteSubscriberFromChannel(Long userId, Long channelId) {
        String key = "Channel:" + channelId;
        basicTemplate.opsForSet().remove(key, userId);
    }

    @Override
    public Set<Long> findSubscribeChannelIdByUserId(Long userId) {
        String key = "User:" + userId;
        return longTemplate.opsForSet().members(key);
    }

    @Override
    public void subscribeToChannel(Long userId, Long channelId) {
        String key = "User:" + userId;
        longTemplate.opsForSet().add(key, channelId);
    }

    @Override
    public void unsubscribeToChannel(Long userId, Long channelId) {
        String key = "User:" + userId;
        longTemplate.opsForSet().remove(key, channelId);
    }

    @Override
    public void unsubscribeToAllChannel(Long userId) {
        String key = "User:" + userId;
        longTemplate.delete(key);
    }

    @Override
    public void saveSubscriptionIdByUserId(String subscriptionId, Long userId) {
        String key = "UserSubscription:" + userId;
        basicTemplate.opsForSet().add(key, subscriptionId);
    }

    @Override
    public Set<String> findSubscriptionIdByUserId(Long userId) {
        String key = "UserSubscription:" + userId;
        return stringTemplate.opsForSet().members(key);
    }

    @Override
    public void deleteSubscriptionIdsByUserId(Long userId) {
        String key = "UserSubscription:" + userId;
        basicTemplate.delete(key);
    }

    @Override
    public Optional<Long> findChannelIdBySubscriptionId(String subscriptionId) {
        String key = "Subscription:" + subscriptionId;
        return Optional.ofNullable(longTemplate.opsForValue().get(key));
    }

    @Override
    public void setSubscriptionIdToChannel(String subscriptionId, Long channelId) {
        String key = "Subscription:" + subscriptionId;
        longTemplate.opsForValue().set(key, channelId);
    }

    @Override
    public void deleteSubscriptionId(String subscriptionId) {
        String key = "Subscription:" + subscriptionId;
        longTemplate.delete(key);
    }
}
