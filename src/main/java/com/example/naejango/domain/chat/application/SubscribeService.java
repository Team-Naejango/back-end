package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.repository.SubscribeRepository;
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

    public void disconnect(Long userId, String sessionId) {
        System.out.println("SubscribeService.disconnect");
        // 유저가 구독한 채널을 조회합니다.
        Set<Long> channelIds = subscribeRepository.findSubscribeChannelIdByUserId(userId);

        // 채널에서 해당 유저를 삭제합니다.
        channelIds.forEach(channelId -> subscribeRepository.deleteSubscriberFromChannel(userId, channelId));

        // 유저의 구독 정보 삭제합니다.
        subscribeRepository.unsubscribeToAllChannel(userId);

        // 구독 정보를 삭제합니다.
        subscribeRepository.findSubscriptionIdByUserId(userId).forEach(subscribeRepository::deleteSubscriptionId);
        subscribeRepository.deleteSubscriptionIdsByUserId(userId);

        // 세션 정보를 삭제합니다.
        subscribeRepository.deleteSessionId(sessionId);
    }

    public void unsubscribe(Long userId, String subscriptionId) {
        // 구독 id 로 구독 채널을 식별합니다.
        Long channelId = subscribeRepository.findChannelIdBySubscriptionId(subscriptionId).orElse(null);
        if(channelId == null) return;

        // 채널에서 구독자 삭제
        subscribeRepository.deleteSubscriberFromChannel(userId, channelId);

        // 사용자의 구독 채널 삭제
        subscribeRepository.unsubscribeToChannel(userId, channelId);

        // 구독 id 삭제
        subscribeRepository.deleteSubscriptionId(subscriptionId);
    }

    public void subscribe(Long userId, String subscriptionId, Long channelId) {
        // 유저를 채널에 등록합니다.
        subscribeRepository.subscribeToChannel(userId, channelId);

        // 채널에 유저를 등록합니다.
        subscribeRepository.setSubscriberToChannel(userId, channelId);

        // 구독 id 가 어떤 채널을 가르키는지 저장합니다.
        subscribeRepository.setSubscriptionIdToChannel(subscriptionId, channelId);

        // 구독 id 를 등록합니다.
        subscribeRepository.saveSubscriptionIdByUserId(subscriptionId, userId);
    }

    public boolean isSubscriber(Long userId, Long channelId) {
        Set<Long> subscribers = subscribeRepository.findSubscribersByChannelId(channelId);
        return subscribers.contains(userId);
    }
}
