package com.example.naejango.domain.chat.application;

import com.example.naejango.domain.chat.repository.SubscribeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SubscribeRepository subscribeRepository;

    public void disconnect(Long userId) {
        Set<Long> channelIds = subscribeRepository.findSubscribeChannelIdByUserId(userId);
        channelIds.forEach(value -> subscribeRepository.stopPublishingToUser(userId, value));
        subscribeRepository.disconnectUser(userId);
    }

    public void unsubscribe(Long userId, String subscriptionId) {
        Long channelId = subscribeRepository.findChannelIdBySubscriptionId(subscriptionId).orElse(null);
        if(channelId == null) return;
        subscribeRepository.stopPublishingToUser(userId, channelId);
        subscribeRepository.unsubscribeToChannel(userId, channelId);
        subscribeRepository.deleteSubscriptionId(subscriptionId);
    }

    public void subscribe(Long userId, String subscriptionId, Long channelId) {
        subscribeRepository.subscribeToChannel(userId, channelId);
        subscribeRepository.startPublishingToUser(userId, channelId);
        subscribeRepository.setSubscriptionIdToChannel(subscriptionId, channelId);
    }

    public boolean isSubscriber(Long userId, Long channelId) {
        Set<Long> subscribers = subscribeRepository.findSubscribersByChannelId(channelId);
        System.out.println("subscribers = " + subscribers);
        return subscribers.contains(userId);
    }

}
