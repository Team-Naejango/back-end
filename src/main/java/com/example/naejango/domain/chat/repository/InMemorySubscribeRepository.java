package com.example.naejango.domain.chat.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Repository
@ConditionalOnProperty(name = "redis-config.websocket", havingValue = "false")
@RequiredArgsConstructor
public class InMemorySubscribeRepository implements SubscribeRepository {

    /* sessionId 로 UserId 저장합니다. */
    private final ConcurrentHashMap<String, Long> sessionIdUserIdMap = new ConcurrentHashMap<>();

    /* 채널을 구독하는 userId 를 저장합니다. */
    private final ConcurrentHashMap<Long, Set<Long>> subscribersInfo = new ConcurrentHashMap<>();

    /* user 가 구독하는 채팅 channelId 를 저장합니다. */
    private final ConcurrentHashMap<Long, Set<Long>> subscribingChannelsInfo = new ConcurrentHashMap<>();

    /* subscriptionId 가 어떤 channel 을 가르키는지 저장합니다. */
    private final ConcurrentHashMap<String, Long> subscriptionIdInfo = new ConcurrentHashMap<>();

    /* user 의 SubscriptionId 를 저장합니다. */
    private final ConcurrentHashMap<Long, Set<String>> userIdSubscriptionIdMap = new ConcurrentHashMap<>();

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
    public void deleteSubscriptionIdsByUserId(Long userId) {
        userIdSubscriptionIdMap.remove(userId);
    }

    @Override
    public Set<Long> findSubscribeChannelIdByUserId(Long userId) {
        return subscribingChannelsInfo.getOrDefault(userId, new HashSet<>());
    }

    @Override
    public Set<Long> findSubscribersByChannelId(Long channelId) {
        return subscribersInfo.getOrDefault(channelId, new HashSet<>());
    }

    @Override
    public Optional<Long> findChannelIdBySubscriptionId(String subscriptionId) {
        return Optional.ofNullable(subscriptionIdInfo.get(subscriptionId));
    }

    @Override
    public void saveSubscriptionIdByUserId(String subscriptionId, Long userId) {
        userIdSubscriptionIdMap.computeIfAbsent(userId, k -> new HashSet<>()).add(subscriptionId);
    }

    @Override
    public Set<String> findSubscriptionIdByUserId(Long userId) {
        return userIdSubscriptionIdMap.getOrDefault(userId, new HashSet<>());
    }

    @Override
    public void setSubscriberToChannel(Long userId, Long channelId) {
        Set<Long> usersId = subscribersInfo.get(channelId);
        if (usersId == null) {
            subscribersInfo.put(channelId, new HashSet<>(Collections.singletonList(userId)));
        }
        else usersId.add(userId);
        System.out.println(subscribersInfo.get(channelId));
    }

    @Override
    public void subscribeToChannel(Long userId, Long channelId) {
        Set<Long> channelsId = subscribingChannelsInfo.get(userId);
        if (channelsId == null) {
            subscribingChannelsInfo.put(userId, new HashSet<>(Collections.singletonList(channelId)));
        }
        else channelsId.add(channelId);
    }

    @Override
    public void setSubscriptionIdToChannel(String subscriptionId, Long channelId) {
        subscriptionIdInfo.put(subscriptionId,channelId);
    }

    @Override
    public void deleteSubscriberFromChannel(Long userId, Long channelId) {
        Set<Long> subscribersId = subscribersInfo.get(channelId);
        if (subscribersId == null) return;
        else subscribersId.remove(userId);
        if (subscribersId.isEmpty()) subscribersInfo.remove(channelId);
    }

    @Override
    public void unsubscribeToChannel(Long userId, Long channelId) {
        Set<Long> channelsId = subscribingChannelsInfo.get(userId);
        if (channelsId == null) return;
        else subscribingChannelsInfo.remove(userId);
        if (channelsId.isEmpty()) subscribingChannelsInfo.remove(channelId);
    }

    @Override
    public void unsubscribeToAllChannel(Long userId) {
        Set<Long> channelsId = subscribingChannelsInfo.get(userId);
        if (channelsId != null) channelsId.forEach(channelId -> setSubscriberToChannel(userId, channelId));
        subscribingChannelsInfo.remove(userId);
    }

    @Override
    public void deleteSubscriptionId(String subscriptionId) {
        subscriptionIdInfo.remove(subscriptionId);
    }
}
