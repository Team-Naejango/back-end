package com.example.naejango.domain.chat.config;

import com.example.naejango.domain.chat.application.ChannelService;
import com.example.naejango.global.auth.jwt.JwtValidator;
import com.example.naejango.global.common.exception.CustomException;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.WebSocketException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtValidator jwtValidator;
    private final ChannelService channelService;

    /**
     * 구독 정보를 저장하는 ConcurrentHashMap 입니다.
     * 구독 신청이나 메세지 전송의 경우 항상 토큰 정보를 검증하기 때문에
     * UserId로 사용자를 식별할 수 있지만 구독 취소 요청의 경우
     * 별다른 토큰 정보 없이도 구독을 취소할 수 있도록
     * UserId 가 아닌 Session Id 를 식별자로 사용합니다.
     * SessionId : (userId, subscribeId, channelId)
     */
    ConcurrentMap<String, SubscriptionInfo> subscriptionInfoMap = new ConcurrentHashMap<>();

    /**
     * 웹소켓 EndPoint 로 전송되는 메세지를 Intercept 하여
     * 사용자 인증, 구독 취소 등의 기능을 수행합니다.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        accessor.setLeaveMutable(true);

        /** 구독 취소 요청 */
        if(accessor.getCommand().equals(StompCommand.UNSUBSCRIBE)){
            Message unsubscribeMessage = createUnsubscribeMessage(accessor);
            channel.send(unsubscribeMessage);
            return message;
        }

        /** 웹소켓 연결 종료 요청 */
        if (accessor.getCommand().equals(StompCommand.DISCONNECT)) {
            if (subscriptionInfoMap.containsKey(accessor.getSessionId())) {
                channel.send(createUnsubscribeMessage(accessor));
            }
            return message;
        }

        /** 여기서 부터는 보안이 필요한 로직을 다룹니다. **/
        /** 웹소켓 연결 요청 : 토큰 검증 */
        if (accessor.getCommand().equals(StompCommand.CONNECT)) {
            validateTokenAndGetId(accessor);
            if(subscriptionInfoMap.containsKey(accessor.getSessionId())) throw new WebSocketException(ErrorCode.SESSION_ALREADY_EXIST);
            return message;
        }

        /** 구독 요청 : 토큰 검증 및 채널 입장 권한 검증 */
        if (accessor.getCommand().equals(StompCommand.SUBSCRIBE)) {
            // 기본적인 토큰 검증
            Long userId = validateTokenAndGetId(accessor);

            // 정보 수신 채널 구독
            if(accessor.getDestination().equals("/user/queue/info")) {
                return messageWithUserIdHeader(message, accessor, userId);
            }

            // 채팅 채널 구독
            Long channelId = getChannelId(accessor);
            if(!channelService.isParticipants(channelId, userId)) throw new WebSocketException(ErrorCode.UNAUTHORIZED_SUBSCRIBE_REQUEST);

            // 구독 정보 등록
            subscriptionInfoMap.compute(accessor.getSessionId(), (sessionId, Info) -> {
                if(Info != null) throw new WebSocketException(ErrorCode.SESSION_ALREADY_EXIST);
                return new SubscriptionInfo(userId, accessor.getSubscriptionId(), channelId);
            });

            return messageWithUserIdHeader(message, accessor, userId);
        }

        /** 메세지 전송 요청 : 토큰 검증 및 메세지 발송 권한 인증*/
        if (accessor.getCommand().equals(StompCommand.SEND) || accessor.getCommand().equals(StompCommand.MESSAGE)) {
            // 구독 취소 요청
            if(accessor.getDestination().equals("/unsubscribe")) return message;

            // 발송 권한 확인 - 이미 채널을 구독 중인 경우에는 토큰 검증하지 않음
            Long channelId = getChannelId(accessor);
            String sessionId = accessor.getSessionId();
            if (subscriptionInfoMap.containsKey(sessionId)) {
                SubscriptionInfo subscriptionInfo = subscriptionInfoMap.get(sessionId);
                if (subscriptionInfo.getChannelId().equals(channelId)) {
                    return messageWithUserIdHeader(message, accessor, subscriptionInfo.getUserId());
                }
            }

            // 기본적인 토큰 검증
            Long userId = validateTokenAndGetId(accessor);

            // 메시지 발송 권한 검증
            if(!channelService.isParticipants(channelId, userId)) throw new WebSocketException(ErrorCode.UNAUTHORIZED_SEND_MESSAGE_REQUEST);
            return messageWithUserIdHeader(message, accessor, userId);
        }

        return null;
    }

    private Message<?> createUnsubscribeMessage(StompHeaderAccessor accessor) {
        StompHeaderAccessor unsubscribeAccessor = StompHeaderAccessor.create(StompCommand.SEND);
        unsubscribeAccessor.setMessageTypeIfNotSet(SimpMessageType.MESSAGE);
        unsubscribeAccessor.setSessionId(accessor.getSessionId());
        unsubscribeAccessor.setSessionAttributes(accessor.getSessionAttributes());
        subscriptionInfoMap.computeIfPresent(accessor.getSessionId(), (sessionId, Info) -> {
            unsubscribeAccessor.setSessionId(accessor.getSessionId());
            unsubscribeAccessor.setHeader("userId", Info.userId);
            unsubscribeAccessor.setSubscriptionId(Info.subscriptionId);
            unsubscribeAccessor.setHeader("channelId", Info.channelId);
            unsubscribeAccessor.setDestination("/unsubscribe");
            return null;
        });
        subscriptionInfoMap.remove(accessor.getSessionId());
        return MessageBuilder.createMessage(new byte[0], unsubscribeAccessor.getMessageHeaders());
    }

    private static Message<?> messageWithUserIdHeader(Message<?> message, StompHeaderAccessor accessor, Long userId) {
        accessor.setHeader("userId", userId);
        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }

    /**
     * header 에 포함된 accessToken 을 복호화 하여 UserId 를 가지고 옵니다.
     * 기본적인 AccessToken 검증을 수행합니다.
     */
    private Long validateTokenAndGetId(StompHeaderAccessor headerAccessor) {
        try {
            var validateRequest = jwtValidator.validateTokenInRequest(headerAccessor);
            if (!validateRequest.isValidToken()) throw new WebSocketException(ErrorCode.ACCESS_TOKEN_NOT_VALID);
            return validateRequest.getUserId();
        } catch (CustomException e) {
            throw new WebSocketException(ErrorCode.ACCESS_TOKEN_NOT_VALID);
        }
    }

    /**
     * 채널 정보를 추출합니다.
     */
    private Long getChannelId(StompHeaderAccessor headerAccessor) {
        String destination = headerAccessor.getDestination();
        if(destination != null && (destination.startsWith("/chat/channel") || destination.startsWith("/topic/channel"))){
            String channelId = destination.substring(destination.lastIndexOf('/') + 1);
            return Long.valueOf(channelId);
        }
        throw new WebSocketException(ErrorCode.UNIDENTIFIED_DESTINATION);
    }


    @Getter
    @AllArgsConstructor
    @ToString
    private static class SubscriptionInfo {
        private Long userId;
        private String subscriptionId;
        private Long channelId;
    }

}