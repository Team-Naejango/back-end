package com.example.naejango.domain.chat.config;

import com.example.naejango.domain.chat.application.ChannelService;
import com.example.naejango.domain.chat.application.WebSocketService;
import com.example.naejango.domain.chat.repository.SubscribeRepository;
import com.example.naejango.global.auth.jwt.JwtAuthenticator;
import com.example.naejango.global.common.exception.ErrorCode;
import com.example.naejango.global.common.exception.WebSocketException;
import com.example.naejango.global.common.handler.AuthenticationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.DefaultSimpUserRegistry;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@SuppressWarnings("NullableProblems")
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final ChannelService channelService;
    private final AuthenticationHandler authenticationHandler;
    private final WebSocketService webSocketService;
    private final JwtAuthenticator jwtAuthenticator;
    private final SubscribeRepository subscribeRepository;
    private final DefaultSimpUserRegistry userRegistry = new DefaultSimpUserRegistry();


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

        /* 이미 인증된 회원인 경우 Authentication 객체를 넣어줍니다. */
        authenticate(accessor);

        /* 웹소켓 연결 요청 */
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 이미 세션에 유저가 할당되어 있는 회원은 예외 반환
            if(accessor.getUser() != null) throw new WebSocketException(ErrorCode.SESSION_ALREADY_EXIST);

            // 인증 처리
            Authentication authentication = jwtAuthenticator.authenticateWebSocketRequest(accessor);
            Long userId = authenticationHandler.userIdFromAuthentication(authentication);
            userRegistry.onApplicationEvent(
                    new SessionConnectedEvent(
                            this, MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders()), authentication));

            // session 을 저장합니다.
            webSocketService.connect(userId, accessor.getSessionId());
            return generateMessage(message, accessor);
        }

        /* 구독 요청(Channel 에 연결된 Chat 객체가 있는지 확인)  */
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // 인증 객체에서 userId 를 꺼내옵니다.
            Long userId = authenticationHandler.userIdFromPrincipal(accessor.getUser());

            // 에러 수신 채널 구독
            if("/user/queue/info".equals(accessor.getDestination())) return generateMessage(message, accessor);

            // 채널 구독 권한 확인
            Long channelId = getChannelId(accessor);
            if(!channelService.isParticipants(channelId, userId)) throw new WebSocketException(ErrorCode.UNAUTHORIZED_SUBSCRIBE_REQUEST);
            return generateMessage(message, accessor);
        }

        /* 메세지 전송 요청 : 메세지 발송 권한 인증(구독 여부 확인) */
        if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.MESSAGE.equals(accessor.getCommand())) {
            // 발송 권한 확인
            Long userId = authenticationHandler.userIdFromPrincipal(accessor.getUser());
            Long channelId = getChannelId(accessor);
            if (!webSocketService.isSubscriber(userId, channelId)) throw new WebSocketException(ErrorCode.UNAUTHORIZED_SEND_MESSAGE_REQUEST);
            return generateMessage(message, accessor);
        }

        /* 구독 취소 요청 */
        if(StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())){
            // subscriptionId 정보 받아오기
            Long userId = authenticationHandler.userIdFromPrincipal(accessor.getUser());
            String subscriptionId = accessor.getSubscriptionId();

            // 해당 subscription 제거
            webSocketService.unsubscribe(userId, subscriptionId);
            return generateMessage(message, accessor);
        }

        /* 웹소켓 연결 종료 요청 */
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            // 모든 구독 정보 삭제
            Long userId = authenticationHandler.userIdFromPrincipal(accessor.getUser());
            webSocketService.disconnect(userId, accessor.getSessionId());

            return message;
        }

        return null;
    }

    /* sessionId 로 User 를 로드합니다. */
    private void authenticate(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        subscribeRepository.findUserIdBySessionId(sessionId).ifPresent(userId -> {
            SimpUser user = userRegistry.getUser(String.valueOf(userId));
            if(user != null) accessor.setUser(user.getPrincipal());
        });
    }

    /* 메세지에 userId 헤더를 담습니다. */
    private Message<?> generateMessage(Message<?> message, StompHeaderAccessor accessor) {
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