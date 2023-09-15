package com.example.naejango.domain.chat.application.websocket;

import com.example.naejango.domain.chat.repository.SubscribeRepository;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.WebSocketException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;


/**
 * 구독 정보를 관리하는 클래스 입니다.
 * 채널의 구독자 정보, 구독자의 구독 채널 정보,
 *
 */
@Service
@RequiredArgsConstructor
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;

    public void disconnect(String sessionId) {
        // 유저 로드
        Long userId = subscribeRepository.findUserIdBySessionId(sessionId)
                .orElseThrow(() -> new WebSocketException(ErrorCode.SESSION_NOT_FOUND));
        
        // 유저가 구독한 채널에서 유저를 삭제합니다.
        Set<Long> channelIds = subscribeRepository.findSubscribeChannelIdBySessionId(sessionId);
        channelIds.forEach(channelId -> subscribeRepository.deleteSubscriberFromChannel(userId, channelId));

        // 구독 정보를 삭제합니다.
        subscribeRepository.findSubscriptionIdBySessionId(sessionId).forEach(subscribeRepository::deleteSubscriptionId);
        subscribeRepository.deleteAllSubscriptionsBySessionId(sessionId);

        // 세션 정보를 삭제합니다.
        subscribeRepository.deleteSessionId(sessionId);
    }

    public void unsubscribe(String sessionId, String subscriptionId) {
        // 구독 id 로 구독 채널을 식별합니다.
        Long channelId = subscribeRepository.findChannelIdBySubscriptionId(subscriptionId).orElse(null);
        if(channelId == null) return;
        
        // 유저 로드
        Long userId = subscribeRepository.findUserIdBySessionId(sessionId)
                .orElseThrow(() -> new WebSocketException(ErrorCode.USER_NOT_FOUND));
        
        // 채널에서 구독자 삭제
        subscribeRepository.deleteSubscriberFromChannel(userId, channelId);

        // 구독 id 삭제
        subscribeRepository.deleteSubscriptionId(subscriptionId);
        
    }

    public void subscribe(Long userId, String sessionId, String subscriptionId, Long channelId) {
        // 채널에 유저를 등록합니다.
        subscribeRepository.setSubscriberToChannel(userId, channelId);

        // 구독 id 가 어떤 채널을 가르키는지 저장합니다.
        subscribeRepository.setSubscriptionIdToChannel(subscriptionId, channelId);

        // 구독 id 를 등록합니다.
        subscribeRepository.saveSubscriptionIdBySessionId(subscriptionId, sessionId);
    }

    public boolean isSubscriber(Long userId, Long channelId) {
        Set<Long> subscribers = subscribeRepository.findSubscribersByChannelId(channelId);
        return subscribers.contains(userId);
    }
}
