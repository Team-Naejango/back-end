package com.example.naejango.domain.chat.repository;

import java.util.Optional;
import java.util.Set;

public interface SubscribeRepository {
    Set<Long> findSubscribersByChannelId(Long channelId);
    Set<Long> findSubscribeChannelIdByUserId(Long userId);

    void subscribeToChannel(Long userId, Long channelId);
    void unsubscribeToChannel(Long userId, Long channelId);
    Optional<Long> findChannelIdBySubscriptionId(String subscriptionId);
    void stopPublishingToUser(Long userId, Long channelId);
    void disconnectUser(Long userId);

    void startPublishingToUser(Long userId, Long channelId);

    void setSubscriptionIdToChannel(String subscriptionId, Long channelId);

    void deleteSubscriptionId(String subscriptionId);
}
