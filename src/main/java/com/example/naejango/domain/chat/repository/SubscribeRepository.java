package com.example.naejango.domain.chat.repository;

import java.util.Optional;
import java.util.Set;

public interface SubscribeRepository {

    /* Session 정보를 관리하는 메서드 */
    void saveUserIdBySessionId(Long userId, String sessionId);
    Optional<Long> findUserIdBySessionId(String sessionId);
    void deleteSessionId(String sessionId);

    /* 채널의 구독자를 관리하는 메서드 */
    Set<Long> findSubscribersByChannelId(Long channelId);
    void startPublishingToUser(Long userId, Long channelId);
    void stopPublishingToUser(Long userId, Long channelId);

    /* 유저의 구독 채널을 관리하는 메서드 */
    Set<Long> findSubscribeChannelIdByUserId(Long userId);
    void subscribeToChannel(Long userId, Long channelId);
    void unsubscribeToChannel(Long userId, Long channelId);
    void unsubscribeToAllChannel(Long userId);

    /* 구독 id 정보를 관리하는 메서드 */
    Optional<Long> findChannelIdBySubscriptionId(String subscriptionId);
    void setSubscriptionIdToChannel(String subscriptionId, Long channelId);
    void deleteSubscriptionId(String subscriptionId);

}
