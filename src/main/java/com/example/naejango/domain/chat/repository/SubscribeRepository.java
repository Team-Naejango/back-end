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
    void setSubscriberToChannel(Long userId, Long channelId);
    void deleteSubscriberFromChannel(Long userId, Long channelId);

    /* 유저의 구독 채널을 관리하는 메서드 */
    Set<Long> findSubscribeChannelIdByUserId(Long userId);
    void subscribeToChannel(Long userId, Long channelId);
    void unsubscribeToChannel(Long userId, Long channelId);
    void unsubscribeToAllChannel(Long userId);

    /* 세션의 구독 id 를 관리하는 메서드 */
    void saveSubscriptionIdByUserId(String subscriptionId, Long userId);
    Set<String> findSubscriptionIdByUserId(Long userId);
    void deleteSubscriptionIdsByUserId(Long userId);

    /* 구독 id 의 채널 정보를 관리하는 메서드 */
    Optional<Long> findChannelIdBySubscriptionId(String subscriptionId);
    void setSubscriptionIdToChannel(String subscriptionId, Long channelId);
    void deleteSubscriptionId(String subscriptionId);

}
