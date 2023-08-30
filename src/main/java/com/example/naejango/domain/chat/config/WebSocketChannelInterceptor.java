package com.example.naejango.domain.chat.config;

import com.example.naejango.domain.chat.application.ChannelService;
import com.example.naejango.domain.chat.application.WebSocketService;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.WebSocketException;
import com.example.naejango.global.common.handler.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@SuppressWarnings("NullableProblems")
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final ChannelService channelService;
    private final AuthenticationHandler authenticationHandler;
    private final WebSocketService webSocketService;

    /**
     * 웹소켓 EndPoint 로 전송되는 메세지를 Intercept 하여
     *  사용자 인증, 구독 취소 등의 기능을 수행합니다.
     * @param message intercept 한 메세지 정보를 담고 있습니다.
     * @param channel 또다른 message 를 보낼 수 있는 창구 입니다.
     * @return 메서드 로직을 거친 message 를 반환합니다. null 인 경우 메세지는 전송되지 않습니다.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        accessor.setLeaveMutable(true);

        /* 구독 취소 요청 */
        if(accessor.getCommand().equals(StompCommand.UNSUBSCRIBE)){
            // subscriptionId 정보 받아오기
            Long userId = authenticationHandler.userIdFromPrincipal(accessor.getUser());
            String subscriptionId = accessor.getSubscriptionId();

            // 해당 subscription 제거
            webSocketService.unsubscribe(userId, subscriptionId);
            return message;
        }

        /* 웹소켓 연결 종료 요청 */
        if (accessor.getCommand().equals(StompCommand.DISCONNECT)) {
            // 모든 구독 정보 삭제
            Long userId = authenticationHandler.userIdFromPrincipal(accessor.getUser());
            webSocketService.disconnect(userId);
            return message;
        }

        /* 웹소켓 연결 요청 : handshake 시에 Security Filter 에서 Authentication 객체가 생성되며, 그 이후 요청에서 식별 가능 */
        if (accessor.getCommand().equals(StompCommand.CONNECT)) {
            return message;
        }

        /* 구독 요청(Channel 에 연결된 Chat 객체가 있는지 확인)  */
        if (accessor.getCommand().equals(StompCommand.SUBSCRIBE)) {
            Long userId = authenticationHandler.userIdFromPrincipal(accessor.getUser());

            // 에러 수신 채널 구독
            if(accessor.getDestination().equals("/user/queue/info")) return messageWithUserIdHeader(message, accessor, userId);

            // 채널 구독 권한 확인
            Long channelId = getChannelId(accessor);
            if(!channelService.isParticipants(channelId, userId)) throw new WebSocketException(ErrorCode.UNAUTHORIZED_SUBSCRIBE_REQUEST);

            // 구독 정보 등록
            String subscriptionId = accessor.getSubscriptionId();
            webSocketService.subscribe(userId, subscriptionId, channelId);
            return messageWithUserIdHeader(message, accessor, userId);
        }

        /* 메세지 전송 요청 : 메세지 발송 권한 인증(구독 여부 확인) */
        if (accessor.getCommand().equals(StompCommand.SEND) || accessor.getCommand().equals(StompCommand.MESSAGE)) {
            // 발송 권한 확인
            Long userId = authenticationHandler.userIdFromPrincipal(accessor.getUser());
            Long channelId = getChannelId(accessor);
            if (!webSocketService.isSubscriber(userId, channelId)) throw new WebSocketException(ErrorCode.UNAUTHORIZED_SEND_MESSAGE_REQUEST);

            return messageWithUserIdHeader(message, accessor, userId);
        }
        return null;
    }

    /* 메세지에 userId 헤더를 담습니다. */
    private Message<?> messageWithUserIdHeader(Message<?> message, StompHeaderAccessor accessor, Long userId) {
        accessor.setHeader("userId", userId);
        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }


    /* 채널 정보를 추출합니다. */
    private Long getChannelId(StompHeaderAccessor headerAccessor) {
        String destination = headerAccessor.getDestination();
        if(destination != null && (destination.startsWith("/chat/channel") || destination.startsWith("/topic/channel"))){
            String channelId = destination.substring(destination.lastIndexOf('/') + 1);
            return Long.valueOf(channelId);
        }
        throw new WebSocketException(ErrorCode.UNIDENTIFIED_DESTINATION);
    }

}