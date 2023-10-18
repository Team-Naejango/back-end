package com.example.naejango.domain.chat.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Repository
@ConditionalOnProperty(name = "redis-config.websocket", havingValue = "false")
@RequiredArgsConstructor
public class InMemorySubscribeRepository implements SubscribeRepository {

    /* sessionId 에 UserId 저장합니다. */
    private final ConcurrentHashMap<String, Long> sessionIdUserIdMap = new ConcurrentHashMap<>();

    /* 채널을 구독하는 userId 를 저장합니다. */
    private final ConcurrentHashMap<Long, Set<Long>> channelSubscribersMap = new ConcurrentHashMap<>();

    /* subscriptionId 가 어떤 channel 을 가르키는지 저장합니다. */
    private final ConcurrentHashMap<String, Long> subscriptionIdChannelIdMap = new ConcurrentHashMap<>();

    /* session 에 SubscriptionId 를 저장합니다. */
    private final ConcurrentHashMap<String, Set<String>> sessionIdSubscriptionIdMap = new ConcurrentHashMap<>();

    @Override
    public void saveUserIdBySessionId(Long userId, String sessionId) {
        sessionIdUserIdMap.put(sessionId, userId);
    }

    @Override
    public Optional<Long> findUserIdBySessionId(String sessionId) {
        return Optional.ofNullable(sessionIdUserIdMap.get(sessionId));
    }

    @Override
    public void deleteSessionId(String sessionId) {
        sessionIdUserIdMap.remove(sessionId);
    }

    @Override
    public void saveSubscriptionIdBySessionId(String subscriptionId, String sessionId) {
        sessionIdSubscriptionIdMap.computeIfAbsent(sessionId, key -> new HashSet<>()).add(subscriptionId);
    }

    @Override
    public Set<String> findSubscriptionIdBySessionId(String sessionId) {
        return sessionIdSubscriptionIdMap.get(sessionId);
    }

    @Override
    public Set<Long> findSubscribeChannelIdBySessionId(String sessionId) {
        return sessionIdSubscriptionIdMap.get(sessionId)
                .stream().map(subscriptionIdChannelIdMap::get).collect(Collectors.toSet());
    }

    @Override
    public void deleteAllSubscriptionsBySessionId(String sessionId) {
        sessionIdSubscriptionIdMap.remove(sessionId);
    }


    @Override
    public Set<Long> findSubscribersByChannelId(Long channelId) {
        return channelSubscribersMap.getOrDefault(channelId, new HashSet<>());
    }

    @Override
    public Optional<Long> findChannelIdBySubscriptionId(String subscriptionId) {
        return Optional.ofNullable(subscriptionIdChannelIdMap.get(subscriptionId));
    }

    @Override
    public void setSubscriberToChannel(Long userId, Long channelId) {
        Set<Long> usersId = channelSubscribersMap.get(channelId);
        if (usersId == null) {
            channelSubscribersMap.put(channelId, new HashSet<>(Collections.singletonList(userId)));
        }
        else usersId.add(userId);
    }

    @Override
    public void setSubscriptionIdToChannel(String subscriptionId, Long channelId) {
        subscriptionIdChannelIdMap.put(subscriptionId,channelId);
    }

    @Override
    public void deleteSubscriberFromChannel(Long userId, Long channelId) {
        Set<Long> subscribersId = channelSubscribersMap.get(channelId);
        if (subscribersId == null) return;
        else subscribersId.remove(userId);
        if (subscribersId.isEmpty()) channelSubscribersMap.remove(channelId);
    }

    @Override
    public void deleteSubscriptionId(String subscriptionId) {
        subscriptionIdChannelIdMap.remove(subscriptionId);
    }

    @Override
    public void deleteSubscriptionIdBySessionId(String subscriptionId, String sessionId) {
        sessionIdSubscriptionIdMap.computeIfPresent(sessionId, (k, m) -> {
            m.remove(subscriptionId);
            return m;
        });
    }
}
