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

    /* 세션의 구독 채널을 관리하는 메서드 */
    Set<Long> findSubscribeChannelIdBySessionId(String sessionId);

    /* 세션의 구독 id 를 관리하는 메서드 */
    void saveSubscriptionIdBySessionId(String subscriptionId, String sessionId);
    void deleteSubscriptionIdBySessionId(String subscriptionId, String sessionId);
    Set<String> findSubscriptionIdBySessionId(String sessionId);
    void deleteAllSubscriptionsBySessionId(String sessionId);

    /* 구독 id 의 채널 정보를 관리하는 메서드 */
    Optional<Long> findChannelIdBySubscriptionId(String subscriptionId);
    void setSubscriptionIdToChannel(String subscriptionId, Long channelId);
    void deleteSubscriptionId(String subscriptionId);

}
