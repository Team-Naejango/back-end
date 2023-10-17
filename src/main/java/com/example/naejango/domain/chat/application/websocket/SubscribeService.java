package com.example.naejango.domain.chat.application.websocket;

import com.example.naejango.domain.chat.dto.SubScribeCommandDto;
import com.example.naejango.domain.chat.repository.ChatRepository;
import com.example.naejango.domain.chat.repository.SubscribeRepository;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.WebSocketException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


/**
 * 구독 정보를 관리하는 클래스
 * 채널의 구독자 정보, 구독자의 구독 채널 정보,
 */
@Service
@RequiredArgsConstructor
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final ChatRepository chatRepository;

    @Transactional
    public void disconnect(String sessionId) {
        // 유저 로드
        Long userId = subscribeRepository.findUserIdBySessionId(sessionId)
                .orElseThrow(() -> new WebSocketException(ErrorCode.SESSION_NOT_FOUND));

        // 유저가 구독하고 있는 채널Id 로드
        Set<Long> channelIds = subscribeRepository.findSubscribeChannelIdBySessionId(sessionId);

        // 유저가 구독한 채널에서 유저를 삭제합니다.
        channelIds.forEach(channelId -> subscribeRepository.deleteSubscriberFromChannel(userId, channelId));

        // 구독 ID 의 채널 정보를 삭제합니다.
        subscribeRepository.findSubscriptionIdBySessionId(sessionId)
                .forEach(subscribeRepository::deleteSubscriptionId);

        // 세션의 구독 정보를 삭제합니다.
        subscribeRepository.deleteAllSubscriptionsBySessionId(sessionId);

        // 롤백 테스트용
//        rollback(sessionId, "disconnect");

        // 세션의 유저 정보를 삭제합니다.
        subscribeRepository.deleteSessionId(sessionId);
    }

    @Transactional
    public void unsubscribe(String sessionId, String subscriptionId) {
        // 유저 로드
        Long userId = subscribeRepository.findUserIdBySessionId(sessionId)
                .orElseThrow(() -> new WebSocketException(ErrorCode.USER_NOT_FOUND));

        // 구독 채널 로드
        Long channelId = subscribeRepository.findChannelIdBySubscriptionId(subscriptionId)
                .orElseThrow(() -> new WebSocketException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        // 구독 ID 삭제
        subscribeRepository.deleteSubscriptionId(subscriptionId);

        // 롤백 테스트를 위한 코드
//        rollback(sessionId, "unsubscribe");

        // 채널에서 구독자 삭제
        subscribeRepository.deleteSubscriberFromChannel(userId, channelId);

        // 세션에서 구독 Id 삭제
        subscribeRepository.deleteSubscriptionIdBySessionId(subscriptionId, sessionId);
    }

    @Transactional
    public void subscribe(SubScribeCommandDto commandDto) {
        // 구독 권한 확인
        Long channelId = commandDto.getChannelId();
        Long userId = commandDto.getUserId();
        if(chatRepository.findChatByChannelIdAndOwnerId(channelId, userId).isEmpty()){
            throw new WebSocketException(ErrorCode.UNAUTHORIZED_SUBSCRIBE_REQUEST);
        }

        // destination 확인
        String destination = commandDto.getDestination();
        if (destination == null || !destination.startsWith("/sub/channel/")) {
            throw new WebSocketException(ErrorCode.UNIDENTIFIED_DESTINATION);
        }

        // 채널에 유저를 등록합니다.
        subscribeRepository.setSubscriberToChannel(userId, channelId);

        // 구독 id 가 어떤 채널을 가리키는지 저장합니다.
        String subscriptionId = commandDto.getSubscriptionId();
        subscribeRepository.setSubscriptionIdToChannel(subscriptionId, channelId);

        // Transaction 테스트를 위한 예외 설정
//        rollback(commandDto.getSessionId(), "subscribe");

        // 세션에 구독 id 를 등록합니다.
        String sessionId = commandDto.getSessionId();
        subscribeRepository.saveSubscriptionIdBySessionId(subscriptionId, sessionId);

    }

    public boolean isSubscriber(Long userId, Long channelId) {
        Set<Long> subscribers = subscribeRepository.findSubscribersByChannelId(channelId);
        return subscribers.contains(userId);
    }

}

